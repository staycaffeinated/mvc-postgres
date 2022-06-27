/*
 * Copyright 2022 [CopyrightOwner]
 */

package com.fvogel.broadcomcc1.endpoint.userinfo;

import com.fvogel.broadcomcc1.common.AbstractIntegrationTest;
import com.fvogel.broadcomcc1.math.SecureRandomSeries;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class UserInfoControllerIT extends AbstractIntegrationTest {

	@Autowired
	private UserInfoRepository userInfoRepository;

	// This holds sample UserInfoEntityBeans that will be saved to the database
	private List<UserInfoEntityBean> userInfoList = null;

	private final SecureRandomSeries randomSeries = new SecureRandomSeries();

	@BeforeEach
	void setUp() {
		userInfoList = new ArrayList<>();
		userInfoList.add(new UserInfoEntityBean(1L, randomSeries.nextResourceId(), "First UserInfo"));
		userInfoList.add(new UserInfoEntityBean(2L, randomSeries.nextResourceId(), "Second UserInfo"));
		userInfoList.add(new UserInfoEntityBean(3L, randomSeries.nextResourceId(), "Third UserInfo"));
		userInfoList = userInfoRepository.saveAll(userInfoList);
	}

	@AfterEach
	public void tearDownEachTime() {
		userInfoRepository.deleteAll();
	}

	/*
	 * FindById
	 */
	@Nested
	public class ValidateFindById {
		@Test
		void shouldFindUserInfoById() throws Exception {
			UserInfoEntityBean userInfo = userInfoList.get(0);
			String userInfoId = userInfo.getResourceId();

			mockMvc.perform(get(UserInfoRoutes.FIND_ONE_USERINFO, userInfoId)).andExpect(status().isOk())
					.andExpect(jsonPath("$.text", is(userInfo.getText())));

		}
	}

	/*
	 * Create method
	 */
	@Nested
	public class ValidateCreateUserInfo {
		@Test
		void shouldCreateNewUserInfo() throws Exception {
			UserInfo resource = UserInfo.builder().text("I am a new resource").build();

			mockMvc.perform(post(UserInfoRoutes.CREATE_USERINFO).contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(resource))).andExpect(status().isCreated())
					.andExpect(jsonPath("$.text", is(resource.getText())));
		}

		/**
		 * Verify the controller's data validation catches malformed inputs, such as
		 * missing required fields, and returns either 'unprocessable entity' or 'bad
		 * request'.
		 */
		@Test
		void shouldReturn4xxWhenCreateNewUserInfoWithoutText() throws Exception {
			UserInfo resource = UserInfo.builder().build();

			// Oddly, depending on whether the repository uses Postgres or H2, there are two
			// different outcomes. With H2, the controller's @Validated annotation is
			// applied and a 400 status code is returned. With Postgres, the @Validated
			// is ignored and a 422 error occurs when the database catches the invalid data.
			mockMvc.perform(post(UserInfoRoutes.CREATE_USERINFO).content(objectMapper.writeValueAsString(resource)))
					.andExpect(status().is4xxClientError());
		}
	}

	/*
	 * Update method
	 */
	@Nested
	public class ValidateUpdateUserInfo {

		@Test
		void shouldUpdateUserInfo() throws Exception {
			UserInfoEntityBean userInfo = userInfoList.get(0);
			UserInfo resource = new UserInfoBeanToResourceConverter().convert(userInfo);

			mockMvc.perform(put(UserInfoRoutes.UPDATE_USERINFO, userInfo.getResourceId())
					.contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(resource)))
					.andExpect(status().isOk()).andExpect(jsonPath("$.text", is(userInfo.getText())));

		}
	}

	/*
	 * Delete method
	 */
	@Nested
	public class ValidateDeleteUserInfo {
		@Test
		void shouldDeleteUserInfo() throws Exception {
			UserInfoEntityBean userInfo = userInfoList.get(0);

			mockMvc.perform(delete(UserInfoRoutes.DELETE_USERINFO, userInfo.getResourceId())).andExpect(status().isOk())
					.andExpect(jsonPath("$.text", is(userInfo.getText())));
		}
	}
}
