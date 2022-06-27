/*
 * Copyright 2022 [CopyrightOwner]
 */
package com.fvogel.broadcomcc1.endpoint.root;

import org.springframework.stereotype.Service;

/**
 * Empty implementation of a Service
 */
@Service
@SuppressWarnings({"java:S3400"})
// S3400: we'll allow a method to return a constant
public class RootService {

	int doNothing() {
		return 0;
	}
}
