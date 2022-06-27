/*
 * Copyright 2022 [CopyrightOwner]
 */

package com.fvogel.broadcomcc1.endpoint.userinfo;

import lombok.NonNull;
import com.fvogel.broadcomcc1.validation.OnCreate;
import com.fvogel.broadcomcc1.validation.OnUpdate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import com.fvogel.broadcomcc1.math.SecureRandomSeries;

import javax.validation.constraints.Min;
import java.util.List;
import java.util.Optional;
import java.util.Objects;

@Service
@Transactional
public class UserInfoService {

	private final UserInfoRepository userInfoRepository;

	private final ConversionService conversionService;

	private final SecureRandomSeries secureRandom;

	/*
	 * Constructor
	 */
	@Autowired
	public UserInfoService(UserInfoRepository userInfoRepository, ConversionService conversionService,
			SecureRandomSeries secureRandom) {
		this.userInfoRepository = userInfoRepository;
		this.conversionService = conversionService;
		this.secureRandom = secureRandom;
	}

	/*
	 * findAll
	 */
	public List<UserInfo> findAllUserInfos() {
		List<UserInfoEntityBean> resultSet = userInfoRepository.findAll();
		return resultSet.stream().map(ejb -> conversionService.convert(ejb, UserInfo.class)).toList();
	}

	/**
	 * findByResourceId
	 */
	public Optional<UserInfo> findUserInfoByResourceId(String id) {
		Optional<UserInfoEntityBean> optional = userInfoRepository.findByResourceId(id);
		return optional.map(ejb -> conversionService.convert(ejb, UserInfo.class));
	}

	/*
	 * findByText
	 */
	public List<UserInfo> findByText(@NonNull String text, @Min(value = 0) int pageNumber,
			@Min(value = 20) int pageSize) {
		Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(text).ascending());
		Page<UserInfoEntityBean> resultSet = userInfoRepository.findByText(text, pageable);
		return resultSet.stream().map(ejb -> conversionService.convert(ejb, UserInfo.class)).toList();
	}

	/**
	 * Persists a new resource
	 */
	public UserInfo createUserInfo(@NonNull @Validated(OnCreate.class) UserInfo resource) {
		resource.setResourceId(secureRandom.nextResourceId());
		UserInfoEntityBean entityBean = Objects
				.requireNonNull(conversionService.convert(resource, UserInfoEntityBean.class));
		entityBean = userInfoRepository.save(entityBean);
		return conversionService.convert(entityBean, UserInfo.class);
	}

	/**
	 * Updates an existing resource
	 */
	public Optional<UserInfo> updateUserInfo(@NonNull @Validated(OnUpdate.class) UserInfo resource) {
		Optional<UserInfoEntityBean> optional = userInfoRepository.findByResourceId(resource.getResourceId());
		if (optional.isPresent()) {
			UserInfoEntityBean entityBean = optional.get();
			// Copy all mutable fields of the resource into the entity bean
			entityBean.setText(resource.getText());
			// persist the changes
			entityBean = userInfoRepository.save(entityBean);
			return Optional.of(Objects.requireNonNull(conversionService.convert(entityBean, UserInfo.class)));
		}
		return Optional.empty();
	}

	/**
	 * delete
	 */
	public void deleteUserInfoByResourceId(@NonNull String id) {
		userInfoRepository.deleteByResourceId(id);
	}
}
