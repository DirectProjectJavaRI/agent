package org.nhindirect.stagent.policy.impl;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.internet.InternetAddress;

import org.nhindirect.policy.PolicyExpression;

public class DomainPolicyResolver_getPolicyTest
{
	@Test
	public void testGetPolicy_incomingPolicyExists_assertPolicies() throws Exception
	{
		final PolicyExpression expression = mock(PolicyExpression.class);
		final List<PolicyExpression> expressions = Arrays.asList(expression);
		
		final Map<String, Collection<PolicyExpression>> policies = new HashMap<String, Collection<PolicyExpression>>();
		policies.put("testdomain.com", expressions);
		
		final DomainPolicyResolver resolver = new DomainPolicyResolver(policies);
		
		Collection<PolicyExpression> retrievedExpressions =  resolver.getIncomingPolicy(new InternetAddress("me@testdomain.com"));
		
		assertNotNull(retrievedExpressions);
		assertEquals(1, retrievedExpressions.size());
	}
	
	@Test
	public void testGetPolicy_incomingPolicyDoesNotExist_assertEmpty() throws Exception
	{
		final PolicyExpression expression = mock(PolicyExpression.class);
		final List<PolicyExpression> expressions = Arrays.asList(expression);
		
		final Map<String, Collection<PolicyExpression>> policies = new HashMap<String, Collection<PolicyExpression>>();
		policies.put("testdomain.com", expressions);
		
		final DomainPolicyResolver resolver = new DomainPolicyResolver(policies);
		
		Collection<PolicyExpression> retrievedExpressions =  resolver.getIncomingPolicy(new InternetAddress("me@testdomainother.com"));
		
		assertNotNull(retrievedExpressions);
		assertEquals(0, retrievedExpressions.size());
	}
	
	@Test
	public void testGetPolicy_outgoingPolicyExists_assertPolicies() throws Exception
	{
		final PolicyExpression expression = mock(PolicyExpression.class);
		final List<PolicyExpression> expressions = Arrays.asList(expression);
		
		final Map<String, Collection<PolicyExpression>> policies = new HashMap<String, Collection<PolicyExpression>>();
		policies.put("testdomain.com", expressions);
		
		final DomainPolicyResolver resolver = new DomainPolicyResolver(policies);
		
		Collection<PolicyExpression> retrievedExpressions =  resolver.getOutgoingPolicy(new InternetAddress("me@testdomain.com"));
		
		assertNotNull(retrievedExpressions);
		assertEquals(1, retrievedExpressions.size());
	}
	
	@Test
	public void testGetPolicy_outgoingPolicyDoesNotExist_assertEmpty() throws Exception
	{
		final PolicyExpression expression = mock(PolicyExpression.class);
		final List<PolicyExpression> expressions = Arrays.asList(expression);
		
		final Map<String, Collection<PolicyExpression>> policies = new HashMap<String, Collection<PolicyExpression>>();
		policies.put("testdomain.com", expressions);
		
		final DomainPolicyResolver resolver = new DomainPolicyResolver(policies);
		
		Collection<PolicyExpression> retrievedExpressions =  resolver.getOutgoingPolicy(new InternetAddress("me@testdomainother.com"));
		
		assertNotNull(retrievedExpressions);
		assertEquals(0, retrievedExpressions.size());
	}
	
	@Test
	public void testGetPolicy_nullAddress_assertException() throws Exception
	{
		final List<PolicyExpression> expressions = new ArrayList<PolicyExpression>();
		
		final Map<String, Collection<PolicyExpression>> policies = new HashMap<String, Collection<PolicyExpression>>();
		policies.put("testdomain.com", expressions);
		
		final DomainPolicyResolver resolver = new DomainPolicyResolver(policies);
		
		boolean exceptionOccured = false;
		
		try
		{
			resolver.getIncomingPolicy(null);
		}
		catch (IllegalArgumentException e)
		{
			exceptionOccured = true;
		}
		
		assertTrue(exceptionOccured);
	}
}
