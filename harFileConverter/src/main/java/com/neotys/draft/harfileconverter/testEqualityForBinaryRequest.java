package com.neotys.draft.harfileconverter;

import java.util.Arrays;
import java.util.Objects;

import com.neotys.neoload.model.v3.project.userpath.Request;

public class testEqualityForBinaryRequest {

	public static void main(String[] args) {
		
		Request Request1 = Request.builder()
				.name("test")
				.bodyBinary(new String("test").getBytes())
				.build();
		
		Request Request2 = Request.builder()
				.name("test")
				.bodyBinary(new String("test").getBytes())
				.build();
		
		System.out.println("Test Request1.equals(Request2) = " + Request1.equals(Request2) );
		
		boolean testCodeEquals = Objects.equals(Request1.getBodyBinary().get(), Request2.getBodyBinary().get());
		System.out.println("Test Objects.equals = " + testCodeEquals );

		boolean testCodeArraysEquals = Arrays.equals(Request1.getBodyBinary().get(), Request2.getBodyBinary().get());
		System.out.println("Test Arrays equals = " + testCodeArraysEquals );

	}

}
