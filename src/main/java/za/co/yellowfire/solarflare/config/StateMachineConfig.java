package za.co.yellowfire.solarflare.config;

import static za.co.yellowfire.solarflare.model.AppraisalEvents.PUBLISH;
import static za.co.yellowfire.solarflare.model.AppraisalEvents.COMPLETE;
import static za.co.yellowfire.solarflare.model.AppraisalEvents.REVIEW;
import static za.co.yellowfire.solarflare.model.AppraisalEvents.DELETE;

import static za.co.yellowfire.solarflare.model.AppraisalStates.NEW;
import static za.co.yellowfire.solarflare.model.AppraisalStates.PUBLISHED;
import static za.co.yellowfire.solarflare.model.AppraisalStates.EMPLOYEE_COMPLETED;
import static za.co.yellowfire.solarflare.model.AppraisalStates.MANAGER_COMPLETED;
import static za.co.yellowfire.solarflare.model.AppraisalStates.REVIEWED;
import static za.co.yellowfire.solarflare.model.AppraisalStates.DELETED;

import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.event.OnStateEntryEvent;
import org.springframework.statemachine.event.OnStateExitEvent;
import org.springframework.statemachine.event.OnTransitionEvent;
import org.springframework.statemachine.event.StateMachineEvent;
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.transition.TransitionKind;

import za.co.yellowfire.solarflare.model.AppraisalEvents;
import za.co.yellowfire.solarflare.model.AppraisalStates;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

@Configuration
@EnableStateMachine
public class StateMachineConfig extends StateMachineConfigurerAdapter<AppraisalStates, AppraisalEvents> {

    @Bean
    public TestEventListener testEventListener() {
        return new TestEventListener();
    }

    @Bean
    public String stateChartModel() throws IOException {
        ClassPathResource model = new ClassPathResource("statechartmodel.txt");
        InputStream inputStream = model.getInputStream();
        Scanner scanner = new Scanner(inputStream);
        String content = scanner.useDelimiter("\\Z").next();
        scanner.close();
        return content;
    }

    

    @Override
    public void configure(StateMachineStateConfigurer<AppraisalStates, AppraisalEvents> states) throws Exception {
        states
        	.withStates()
        	.initial(NEW)
        	.state(PUBLISHED)
        	.state(EMPLOYEE_COMPLETED)
        	.state(MANAGER_COMPLETED)
        	.state(REVIEWED)
        	.state(DELETED);
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<AppraisalStates, AppraisalEvents> transitions) throws Exception {
        transitions
        	.withExternal()
                .source(NEW).target(PUBLISHED)
                .event(PUBLISH)
                .and()
            .withExternal()
                .source(PUBLISHED).target(EMPLOYEE_COMPLETED)
                .event(COMPLETE)
                .guard(new TestGuard())
                .and()
            .withExternal()
                .source(EMPLOYEE_COMPLETED).target(MANAGER_COMPLETED)
                .event(COMPLETE)
                .and()
            .withExternal()
                .source(MANAGER_COMPLETED).target(REVIEWED)
                .event(REVIEW)
                .and()
            .withExternal()
                .source(NEW).target(DELETED)
                .event(DELETE)
                .and()
            .withExternal()
                .source(PUBLISHED).target(DELETED)
                .event(DELETE)
                ;
    }

    private static class TestGuard implements Guard<AppraisalStates, AppraisalEvents> {
        /**
         * Evaluate a guard condition.
         *
         * @param context the state context
         * @return true, if guard evaluation is successful, false otherwise.
         */
        @Override
        public boolean evaluate(StateContext<AppraisalStates, AppraisalEvents> context) {
            System.out.println("Returning false from guard");
            return true;
        }
    }
    
    
    static class TestEventListener implements ApplicationListener<StateMachineEvent> {

        @Override
        public void onApplicationEvent(StateMachineEvent event) {
            if (event instanceof OnStateEntryEvent) {
                OnStateEntryEvent e = (OnStateEntryEvent)event;
                System.out.println("Entry state " + e.getState().getId());
            } else if (event instanceof OnStateExitEvent) {
                OnStateExitEvent e = (OnStateExitEvent)event;
                System.out.println("Exit state " + e.getState().getId());
            } else if (event instanceof OnTransitionEvent) {
                OnTransitionEvent e = (OnTransitionEvent)event;
                if (e.getTransition().getKind() == TransitionKind.INTERNAL) {
                    System.out.println("Internal transition source=" + e.getTransition().getSource().getId());
                }
            }
        }
    }
}
