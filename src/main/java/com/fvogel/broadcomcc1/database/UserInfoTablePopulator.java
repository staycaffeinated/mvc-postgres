/*
 * Copyright 2022 [CopyrightOwner]
 */

package com.fvogel.broadcomcc1.database;

import com.fvogel.broadcomcc1.endpoint.userinfo.UserInfoEntityBean;
import com.fvogel.broadcomcc1.endpoint.userinfo.UserInfoRepository;
import com.fvogel.broadcomcc1.math.SecureRandomSeries;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * This component loads sample data into the UserInfo database table. This is
 * suitable for testing and demonstration, but probably not for production.
 */
@Component
@Slf4j
public class UserInfoTablePopulator implements ApplicationListener<ApplicationReadyEvent> {

	private final UserInfoRepository repository;
	private final SecureRandomSeries randomSeries;

	/**
	 * Constructor
	 */
	public UserInfoTablePopulator(UserInfoRepository repository, SecureRandomSeries randomSeries) {
		this.repository = repository;
		this.randomSeries = randomSeries;
	}

	/**
	 * Inserts sample data into the UserInfo table
	 */
	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {
		repository.deleteAll();
		List<UserInfoEntityBean> sampleData = createSampleData();
		repository.saveAllAndFlush(sampleData);
	}

	/**
	 * Creates a collection of sample data
	 */
	private List<UserInfoEntityBean> createSampleData() {
		String[] textSamples = {"One", "Two", "Three", "Four", "Five"};
		List<UserInfoEntityBean> list = new ArrayList<>();
		for (String s : textSamples) {
			list.add(createOne(s));
		}
		return list;
	}

	/**
	 * Creates a single UserInfo entity bean
	 */
	private UserInfoEntityBean createOne(String text) {
		UserInfoEntityBean bean = new UserInfoEntityBean();
		bean.setText(text);
		bean.setResourceId(randomSeries.nextResourceId());
		return bean;
	}
}