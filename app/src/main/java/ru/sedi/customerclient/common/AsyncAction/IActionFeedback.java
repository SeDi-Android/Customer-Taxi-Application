package ru.sedi.customerclient.common.AsyncAction;

public interface IActionFeedback<T>
{
	void onResponse(T result);
	void onFailure(Exception e);
}
