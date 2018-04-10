package com.ybnf.dsl.domain;

import java.util.LinkedList;
import java.util.List;

public class Result {
	private String recognized;
	private String remaining;
	private boolean succeeded;
	private List<Result> results;

	protected Result(String recognized, String remaining, boolean succeeded) {
		this.recognized = recognized;
		this.remaining = remaining;
		this.succeeded = succeeded;
		this.results = new LinkedList<>();
	}

	protected Result(Result result) {
		this(result.recognized, result.remaining, result.succeeded);
		this.results = result.results;
	}

	public static Result succeed(String recognized, String remaining) {
		return new Result(recognized, remaining, true);
	}

	public static Result fail() {
		return new Result("", "", false);
	}

	public static Result concat(Result first, Result second) {
		Result result = new Result(first.recognized.concat(second.recognized), second.remaining, true);
		result.results.add(first);
		result.results.add(second);
		return result;
	}

	public String getRecognized() {
		return recognized;
	}

	public String getRemaining() {
		return remaining;
	}

	public boolean isSucceeded() {
		return succeeded;
	}
	
	public List<Result> getResults() {
		return results;
	}

	@Override
	public String toString() {
		return "Result [recognized=" + recognized + ", remaining=" + remaining + ", succeeded=" + succeeded
				+ ", results=" + results + "]";
	}
}
