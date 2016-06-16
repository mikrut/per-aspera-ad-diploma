package ru.mail.park.chat.activities.tasks;

/**
 * Created by Михаил on 16.06.2016.
 */
public interface IOperationListener <OperationResult, OperationFailResult> {
    void onOperationStart();
    void onOperationSuccess(OperationResult result);
    void onOperationFail(OperationFailResult failResult);
}