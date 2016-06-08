package za.co.yellowfire.solarflare.model;

import static za.co.yellowfire.solarflare.model.tables.Appraisal.APPRAISAL;

import java.util.stream.Collectors;

import org.jooq.DSLContext;
import org.jooq.Result;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.transition.Transition;

import za.co.yellowfire.solarflare.model.tables.records.AppraisalRecord;
import za.co.yellowfire.solarflare.statemachine.PersistStateMachineHandler;
import za.co.yellowfire.solarflare.statemachine.PersistStateMachineHandler.PersistStateChangeListener;;

public class Persist {

    private final PersistStateMachineHandler<AppraisalStates, AppraisalEvents> handler;
    private final DSLContext dsl;

    public Persist(final DSLContext dsl, final PersistStateMachineHandler<AppraisalStates, AppraisalEvents> handler) {
        this.dsl = dsl;
    	this.handler = handler;
        this.handler.addPersistStateChangeListener(new LocalPersistStateChangeListener());
    }

    public String listDbEntries() {
    	Result<AppraisalRecord> results = dsl.selectFrom(APPRAISAL).fetch();
    	return results.stream().map(s -> s.toString()).collect(Collectors.joining("\n"));
    }
    
    public void create(int id) throws NotFoundException {
    	
    	dsl.insertInto(APPRAISAL).set(APPRAISAL.ID, id).set(APPRAISAL.STATE, "NEW").execute();
    	
        transitionTo(id, AppraisalEvents.PUBLISH);
    }
    

    public void transitionTo(final int appraisalId, final AppraisalEvents event) throws NotFoundException {
    	AppraisalRecord result = dsl.selectFrom(APPRAISAL).where(APPRAISAL.ID.eq(appraisalId)).fetchOne();
    	
    	if (result != null) {
    		Message<AppraisalEvents> message = MessageBuilder.withPayload(event).setHeader("appraisalId", appraisalId).build();
            handler.handleEventWithState(message, result.getValue(APPRAISAL.STATE, new AppraisalStatesConverter()));
    	} else {
    		throw new NotFoundException("The appraisal " + appraisalId + " was not found.");
    	}
    }


    private class LocalPersistStateChangeListener implements PersistStateChangeListener<AppraisalStates, AppraisalEvents> {

        @Override
        public void onPersist(
        		State<AppraisalStates, AppraisalEvents> state, 
        		Message<AppraisalEvents> message,
                Transition<AppraisalStates, AppraisalEvents> transition, 
                StateMachine<AppraisalStates, AppraisalEvents> stateMachine) {
        	
            if (message != null && message.getHeaders().containsKey("appraisalId")) {
                int appraisalId = message.getHeaders().get("appraisalId", Integer.class);
                dsl.update(APPRAISAL)
                	.set(APPRAISAL.STATE, new AppraisalStatesConverter().to(state.getId()))
                	.where(APPRAISAL.ID.eq(appraisalId))
                	.execute();
            }
        }
    }
}