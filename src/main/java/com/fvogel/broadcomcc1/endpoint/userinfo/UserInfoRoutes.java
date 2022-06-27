/*
 * Copyright 2022 [CopyrightOwner]
 */
package com.fvogel.broadcomcc1.endpoint.userinfo;

/**
 * Routes to UserInfo resources
 */
@SuppressWarnings({"java:S1075"})
// S1075: let basePath be hard-coded
public final class UserInfoRoutes {

	private UserInfoRoutes() {
	}

	public static final String BASE_PATH_USERINFO = "/users";
	public static final String USERINFO_ID = "/{id}";

	public static final String CREATE_USERINFO = BASE_PATH_USERINFO;
	public static final String UPDATE_USERINFO = BASE_PATH_USERINFO + USERINFO_ID;
	public static final String DELETE_USERINFO = BASE_PATH_USERINFO + USERINFO_ID;

	public static final String FIND_ONE_USERINFO = BASE_PATH_USERINFO + USERINFO_ID;
	public static final String FIND_ALL_USERINFO = BASE_PATH_USERINFO + "/findAll";
	public static final String SEARCH_USERINFO = BASE_PATH_USERINFO + "/search";

}