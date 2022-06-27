/*
 * Copyright 2022 [CopyrightOwner]
 */
package com.fvogel.broadcomcc1.endpoint.userinfo;

import com.fvogel.broadcomcc1.exception.UnprocessableEntityException;
import com.fvogel.broadcomcc1.validation.OnCreate;
import com.fvogel.broadcomcc1.validation.OnUpdate;
import com.fvogel.broadcomcc1.validation.ResourceId;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;
import java.util.Optional;
import java.net.URI;

@RestController
@RequestMapping
@Slf4j
@Validated
public class UserInfoController {

	private final UserInfoService userInfoService;

	/*
	 * Constructor
	 */
	@Autowired
	public UserInfoController(UserInfoService userInfoService) {
		this.userInfoService = userInfoService;
	}

	/*
	 * Get all
	 */
	@GetMapping(value = UserInfoRoutes.FIND_ALL_USERINFO, produces = MediaType.APPLICATION_JSON_VALUE)
	public List<UserInfo> getAllUserInfos() {
		return userInfoService.findAllUserInfos();
	}

	/*
	 * Get one by resourceId
	 *
	 */
	@GetMapping(value = UserInfoRoutes.FIND_ONE_USERINFO, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<UserInfo> getUserInfoById(@PathVariable @ResourceId String id) {
		return userInfoService.findUserInfoByResourceId(id).map(ResponseEntity::ok)
				.orElseGet(() -> ResponseEntity.notFound().build());
	}

	/*
	 * Create one
	 */
	@PostMapping(value = UserInfoRoutes.CREATE_USERINFO, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	public ResponseEntity<UserInfo> createUserInfo(@RequestBody @Validated(OnCreate.class) UserInfo resource) {
		try {
			UserInfo savedResource = userInfoService.createUserInfo(resource);
			URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
					.buildAndExpand(savedResource.getResourceId()).toUri();
			return ResponseEntity.created(uri).body(savedResource);
		}
		// if, for example, a database constraint prevents writing the data...
		catch (org.springframework.transaction.TransactionSystemException e) {
			log.error(e.getMessage());
			throw new UnprocessableEntityException();
		}
	}

	/*
	 * Update one
	 */
	@PutMapping(value = UserInfoRoutes.FIND_ONE_USERINFO, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<UserInfo> updateUserInfo(@PathVariable @ResourceId String id,
			@RequestBody @Validated(OnUpdate.class) UserInfo userInfo) {
		if (!id.equals(userInfo.getResourceId())) {
			throw new UnprocessableEntityException("The identifier in the query string and request body do not match");
		}
		Optional<UserInfo> optional = userInfoService.updateUserInfo(userInfo);
		return optional.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
	}

	/*
	 * Delete one
	 */
	@DeleteMapping(value = UserInfoRoutes.FIND_ONE_USERINFO)
	public ResponseEntity<UserInfo> deleteUserInfo(@PathVariable @ResourceId String id) {
		return userInfoService.findUserInfoByResourceId(id).map(userInfo -> {
			userInfoService.deleteUserInfoByResourceId(id);
			return ResponseEntity.ok(userInfo);
		}).orElseGet(() -> ResponseEntity.notFound().build());
	}

	/*
	 * Find by text
	 */
	@GetMapping(value = UserInfoRoutes.SEARCH_USERINFO, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<UserInfo>> searchByText(@RequestParam(name = "text", required = true) String text,
			@RequestParam(name = "page", required = false, defaultValue = "1") int pageNumber,
			@RequestParam(name = "size", required = false, defaultValue = "20") Integer pageSize) {
		return ResponseEntity.ok(userInfoService.findByText(text, pageNumber, pageSize));
	}
}
