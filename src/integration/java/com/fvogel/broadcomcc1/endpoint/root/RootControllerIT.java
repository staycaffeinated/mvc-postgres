/*
 * Copyright 2022 [CopyrightOwner]
 */
package com.fvogel.broadcomcc1.endpoint.root;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fvogel.broadcomcc1.common.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

public class RootControllerIT extends AbstractIntegrationTest {
	@Test
	public void testGetHome() throws Exception {
		mockMvc.perform(get("/")).andExpect(status().isOk());
	}
}