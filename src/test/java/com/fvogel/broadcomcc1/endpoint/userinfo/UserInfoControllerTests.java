/*
 * Copyright 2022 [CopyrightOwner]
 */

package com.fvogel.broadcomcc1.endpoint.userinfo;

import com.fvogel.broadcomcc1.math.SecureRandomSeries;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.TransactionSystemException;
import org.zalando.problem.jackson.ProblemModule;
import org.zalando.problem.violations.ConstraintViolationProblemModule;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.reset;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserInfoController.class)
@ActiveProfiles("test")
class UserInfoControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private UserInfoService userInfoService;

	@Autowired
	private ObjectMapper objectMapper;

	private List<UserInfo> userInfoList;

	private final SecureRandomSeries randomSeries = new SecureRandomSeries();

	@BeforeEach
	void setUp() {
		userInfoList = new ArrayList<>();
		userInfoList.add(UserInfo.builder().resourceId(randomSeries.nextResourceId()).text("text 1").build());
		userInfoList.add(UserInfo.builder().resourceId(randomSeries.nextResourceId()).text("text 2").build());
		userInfoList.add(UserInfo.builder().resourceId(randomSeries.nextResourceId()).text("text 3").build());

		objectMapper.registerModule(new ProblemModule());
		objectMapper.registerModule(new ConstraintViolationProblemModule());
	}

	@AfterEach
	void tearDownEachTime() {
		reset(userInfoService);
	}

	@Nested
	class FindAllTests {
		/*
		 * shouldFetchAllUserInfos
		 */
		@Test
		void shouldFetchAllUserInfos() throws Exception {
			given(userInfoService.findAllUserInfos()).willReturn(userInfoList);

			mockMvc.perform(get(UserInfoRoutes.FIND_ALL_USERINFO)).andExpect(status().isOk())
					.andExpect(jsonPath("$.size()", is(userInfoList.size())));
		}
	}

	@Nested
	class FindByIdTests {
		/*
		 * shouldFindUserInfoById
		 */
		@Test
		void shouldFindUserInfoById() throws Exception {
			// given
			String resourceId = randomSeries.nextResourceId();
			UserInfo userInfo = UserInfo.builder().resourceId(resourceId).text("text 1").build();

			given(userInfoService.findUserInfoByResourceId(resourceId)).willReturn(Optional.of(userInfo));

			// when/then
			mockMvc.perform(get(UserInfoRoutes.FIND_ONE_USERINFO, resourceId)).andExpect(status().isOk())
					.andExpect(jsonPath("$.text", is(userInfo.getText())));
		}

		@Test
		void shouldReturn404WhenFetchingNonExistingUserInfo() throws Exception {
			// given
			String resourceId = randomSeries.nextResourceId();
			given(userInfoService.findUserInfoByResourceId(resourceId)).willReturn(Optional.empty());

			// when/then
			mockMvc.perform(get(UserInfoRoutes.FIND_ONE_USERINFO, resourceId)).andExpect(status().isNotFound());

		}
	}

	@Nested
	class CreateUserInfoTests {
		@Test
		void shouldCreateNewUserInfo() throws Exception {
			// given
			UserInfo resource = UserInfo.builder().text("sample").build();
			UserInfo resourceAfterSave = UserInfo.builder().text("sample").resourceId(randomSeries.nextResourceId())
					.build();
			given(userInfoService.createUserInfo(any(UserInfo.class))).willReturn(resourceAfterSave);

			// when/then
			mockMvc.perform(post(UserInfoRoutes.CREATE_USERINFO).contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(resource))).andExpect(status().isCreated())
					.andExpect(jsonPath("$.resourceId", notNullValue()))
					.andExpect(jsonPath("$.text", is(resourceAfterSave.getText())));
		}

		@Test
		void whenDatabaseThrowsException_expectUnprocessableEntityResponse() throws Exception {
			// given the database throws an exception when the entity is saved
			given(userInfoService.createUserInfo(any(UserInfo.class))).willThrow(TransactionSystemException.class);
			UserInfo resource = UserInfo.builder().build();

			mockMvc.perform(post(UserInfoRoutes.CREATE_USERINFO).contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(resource))).andExpect(status().isUnprocessableEntity());
		}
	}

	@Nested
	class UpdateUserInfoTests {
		@Test
		void shouldUpdateUserInfo() throws Exception {
			// given
			String resourceId = randomSeries.nextResourceId();
			UserInfo userInfo = UserInfo.builder().resourceId(resourceId).text("sample text").build();
			given(userInfoService.findUserInfoByResourceId(resourceId)).willReturn(Optional.of(userInfo));
			given(userInfoService.updateUserInfo(any(UserInfo.class))).willReturn(Optional.of(userInfo));

			// when/then
			mockMvc.perform(put(UserInfoRoutes.UPDATE_USERINFO, userInfo.getResourceId())
					.contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(userInfo)))
					.andExpect(status().isOk()).andExpect(jsonPath("$.text", is(userInfo.getText())));

		}

		@Test
		void shouldReturn404WhenUpdatingNonExistingUserInfo() throws Exception {
			// given
			String resourceId = randomSeries.nextResourceId();
			given(userInfoService.findUserInfoByResourceId(resourceId)).willReturn(Optional.empty());

			// when/then
			UserInfo resource = UserInfo.builder().resourceId(resourceId).text("updated text").build();

			mockMvc.perform(put(UserInfoRoutes.UPDATE_USERINFO, resourceId).contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(resource))).andExpect(status().isNotFound());

		}

		/**
		 * When the Ids in the query string and request body do not match, expect an
		 * 'Unprocessable Entity' response code
		 */
		@Test
		void shouldReturn422WhenIdsMismatch() throws Exception {
			// given
			String resourceId = randomSeries.nextResourceId();
			String mismatchingId = randomSeries.nextResourceId();
			given(userInfoService.findUserInfoByResourceId(resourceId)).willReturn(Optional.empty());

			// when the ID in the request body does not match the ID in the query string...
			UserInfo resource = UserInfo.builder().resourceId(mismatchingId).text("updated text").build();

			// expect an UnprocessableEntity status code
			mockMvc.perform(put(UserInfoRoutes.UPDATE_USERINFO, resourceId).contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(resource))).andExpect(status().isUnprocessableEntity());
		}
	}

	@Nested
	class DeleteUserInfoTests {
		@Test
		void shouldDeleteUserInfo() throws Exception {
			// given
			String resourceId = randomSeries.nextResourceId();
			UserInfo userInfo = UserInfo.builder().resourceId(resourceId).text("delete me").build();
			given(userInfoService.findUserInfoByResourceId(resourceId)).willReturn(Optional.of(userInfo));
			doNothing().when(userInfoService).deleteUserInfoByResourceId(userInfo.getResourceId());

			// when/then
			mockMvc.perform(delete(UserInfoRoutes.DELETE_USERINFO, resourceId)).andExpect(status().isOk())
					.andExpect(jsonPath("$.text", is(userInfo.getText())));
		}

		@Test
		void shouldReturn404WhenDeletingNonExistingUserInfo() throws Exception {
			String resourceId = randomSeries.nextResourceId();
			given(userInfoService.findUserInfoByResourceId(resourceId)).willReturn(Optional.empty());

			mockMvc.perform(delete(UserInfoRoutes.DELETE_USERINFO, resourceId)).andExpect(status().isNotFound());
		}
	}

	@Nested
	class SearchByTextTests {
		@Test
		void shouldReturnListWhenMatchesAreFound() throws Exception {
			given(userInfoService.findByText(anyString(), anyInt(), anyInt())).willReturn(userInfoList);

			// when/then
			mockMvc.perform(
					get(UserInfoRoutes.SEARCH_USERINFO).param("text", "sample").param("page", "1").param("size", "10"))
					.andExpect(status().isOk());
		}
	}
}