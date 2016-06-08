package za.co.yellowfire.solarflare.config;

import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.listener.StateMachineListener;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.transition.Transition;

import za.co.yellowfire.solarflare.model.AppraisalEvents;
import za.co.yellowfire.solarflare.model.AppraisalStates;
import za.co.yellowfire.solarflare.model.Persist;
import za.co.yellowfire.solarflare.statemachine.PersistStateMachineHandler;

@Configuration
public class PersistHandlerConfig {
    @Autowired
    private StateMachine<AppraisalStates, AppraisalEvents> stateMachine;

    @Autowired
    private DSLContext dsl;
    
    @Bean
    public Persist persist() {
        return new Persist(dsl, persistStateMachineHandler());
    }

    @Bean
    public PersistStateMachineHandler<AppraisalStates, AppraisalEvents> persistStateMachineHandler() {
        stateMachine.addStateListener(new StateMachineListener<AppraisalStates, AppraisalEvents>() {
            @Override
            public void stateChanged(State<AppraisalStates, AppraisalEvents> from, State<AppraisalStates, AppraisalEvents> to) {
                System.out.println("State changed " + from + " -> " + to);
            }

            @Override
            public void stateEntered(State<AppraisalStates, AppraisalEvents> state) {
                System.out.println("State entered " + state);
            }

            @Override
            public void stateExited(State<AppraisalStates, AppraisalEvents> state) {
                System.out.println("State exited " + state);
            }

            @Override
            public void eventNotAccepted(Message<AppraisalEvents> event) {
                System.out.println("Not accepted " + event);
            }

            @Override
            public void transition(Transition<AppraisalStates, AppraisalEvents> transition) {
                System.out.println("Transition " + transition);
            }

            @Override
            public void transitionStarted(Transition<AppraisalStates, AppraisalEvents> transition) {

            }

            @Override
            public void transitionEnded(Transition<AppraisalStates, AppraisalEvents> transition) {

            }

            @Override
            public void stateMachineStarted(StateMachine<AppraisalStates, AppraisalEvents> stateMachine) {

            }

            @Override
            public void stateMachineStopped(StateMachine<AppraisalStates, AppraisalEvents> stateMachine) {

            }

            @Override
            public void stateMachineError(StateMachine<AppraisalStates, AppraisalEvents> stateMachine, Exception exception) {
                System.out.println("Machine error " + stateMachine + " : " + exception);
            }

            @Override
            public void extendedStateChanged(Object key, Object value) {

            }
        });
        return new PersistStateMachineHandler<>(stateMachine);
    }
}
