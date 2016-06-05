package ru.mail.park.chat.activities.fragments;

import android.view.View;

/**
 * Created by Михаил on 05.06.2016.
 */
public abstract class FSM <Event, State> {
    private State currentState;
    private FSMListener<State> listener;

    public FSM(State initialState) {
        currentState = initialState;
    }

    public final State handleEvent(Event event) {
        currentState = nextState(event, currentState);
        if (listener != null) {
            listener.onStateChange(currentState);
        }
        return currentState;
    }

    public final State getCurrentState() {
        return currentState;
    }

    public void setListener(FSMListener<State> listener) {
        this.listener = listener;
    }

    protected abstract State nextState(Event event, State currentState);

    public EventEmittingOnClickListener createListener(Event emittedEvent) {
        return new EventEmittingOnClickListener(emittedEvent);
    }

    public class EventEmittingOnClickListener implements View.OnClickListener {
        private Event emittedEvent;
        private Validator validator;

        public EventEmittingOnClickListener(Event emittedEvent) {
            this.emittedEvent = emittedEvent;
        }

        public void setValidator(Validator validator) {
            this.validator = validator;
        }

        @Override
        public void onClick(View v) {
            if (validator != null && !validator.validate()) {
                return;
            }
            handleEvent(emittedEvent);
        }
    }


    public interface Validator {
        boolean validate();
    }

    public interface FSMListener<State> {
        void onStateChange(State newState);
    }
}
