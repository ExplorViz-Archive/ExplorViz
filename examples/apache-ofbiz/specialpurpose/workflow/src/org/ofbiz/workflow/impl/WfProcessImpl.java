/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/
package org.ofbiz.workflow.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.StringUtil;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.service.GenericResultWaiter;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.job.Job;
import org.ofbiz.service.job.JobManager;
import org.ofbiz.service.job.JobManagerException;
import org.ofbiz.workflow.AlreadyRunning;
import org.ofbiz.workflow.CannotChangeRequester;
import org.ofbiz.workflow.CannotStart;
import org.ofbiz.workflow.CannotStop;
import org.ofbiz.workflow.InvalidData;
import org.ofbiz.workflow.InvalidPerformer;
import org.ofbiz.workflow.InvalidState;
import org.ofbiz.workflow.NotRunning;
import org.ofbiz.workflow.ResultNotAvailable;
import org.ofbiz.workflow.WfActivity;
import org.ofbiz.workflow.WfEventAudit;
import org.ofbiz.workflow.WfException;
import org.ofbiz.workflow.WfFactory;
import org.ofbiz.workflow.WfProcess;
import org.ofbiz.workflow.WfProcessMgr;
import org.ofbiz.workflow.WfRequester;
import org.ofbiz.workflow.WfUtil;
import org.ofbiz.workflow.client.StartActivityJob;

/**
 * WfProcessImpl - Workflow Process Object implementation
 */
public class WfProcessImpl extends WfExecutionObjectImpl implements WfProcess {

    public static final String module = WfProcessImpl.class.getName();

    protected WfRequester requester = null;
    protected WfProcessMgr manager = null;

    public WfProcessImpl(GenericValue valueObject, WfProcessMgr manager) throws WfException {
        super(valueObject, null);
        this.manager = manager;
        this.requester = null;
        init();
    }

    /**
     * @see org.ofbiz.workflow.impl.WfExecutionObjectImpl#WfExecutionObjectImpl(org.ofbiz.entity.Delegator, java.lang.String)
     */
    public WfProcessImpl(Delegator delegator, String workEffortId) throws WfException {
        super(delegator, workEffortId);
        if (UtilValidate.isNotEmpty(activityId))
            throw new WfException("Execution object is not of type WfProcess.");
        this.manager = WfFactory.getWfProcessMgr(delegator, packageId, packageVersion, processId, processVersion);
        this.requester = null;
    }

    private void init() throws WfException {
        // since we are a process we don't have a context yet
        // get the context to use with parsing descriptions from the manager
        Map<String, Object> context = manager.getInitialContext();
        this.parseDescriptions(context);
    }

    /**
     * @see org.ofbiz.workflow.WfProcess#setRequester(org.ofbiz.workflow.WfRequester)
     */
    public void setRequester(WfRequester newValue) throws WfException, CannotChangeRequester {
        if (requester != null)
            throw new CannotChangeRequester();
        requester = newValue;
    }

    /**
     * @see org.ofbiz.workflow.WfProcess#getSequenceStep(int)
     */
    public List<WfActivity> getSequenceStep(int maxNumber) throws WfException {
        if (maxNumber > 0)
            return new ArrayList<WfActivity>(activeSteps().subList(0, maxNumber - 1));
        return activeSteps();
    }

    /**
     * @see org.ofbiz.workflow.WfExecutionObject#abort()
     */
    @Override
    public void abort() throws WfException, CannotStop, NotRunning {
        super.abort();

        // cancel the active activities
        for (WfActivity activity : this.activeSteps()) {
            activity.abort();
        }
    }

    /**
     * @see org.ofbiz.workflow.WfProcess#start()
     */
    public void start() throws WfException, CannotStart, AlreadyRunning {
        start(null);
    }

    /**
     * @see org.ofbiz.workflow.WfProcess#start()
     */
    public void start(String activityId) throws WfException, CannotStart, AlreadyRunning {
        if (state().equals("open.running"))
            throw new AlreadyRunning("Process is already running");

        if (activityId == null && getDefinitionObject().get("defaultStartActivityId") == null)
            throw new CannotStart("Initial activity is not defined.");

        changeState("open.running");

        // start the first activity (using the defaultStartActivityId)
        GenericValue start = null;

        try {
            if (activityId != null) {
                GenericValue processDef = getDefinitionObject();
                Map<String, Object> fields = UtilMisc.toMap("packageId", (Object) processDef.getString("packageId"), "packageVersion",
                        processDef.getString("packageVersion"), "processId", processDef.getString("processId"),
                        "processVersion", processDef.getString("processVersion"), "activityId", activityId);
                start = getDelegator().findByPrimaryKey("WorkflowActivity", fields);

                // here we must check and make sure this activity is defined to as a starting activity
                if (!start.getBoolean("canStart").booleanValue())
                    throw new CannotStart("The specified activity cannot initiate the workflow process");
            } else {
                // this is either the first activity defined or specified as an ExtendedAttribute
                // since this is defined in XPDL, we don't care if canStart is set.
                start = getDefinitionObject().getRelatedOne("DefaultStartWorkflowActivity");
            }
        } catch (GenericEntityException e) {
            throw new WfException(e.getMessage(), e.getNested());
        }
        if (start == null)
            throw new CannotStart("No initial activity available");

        if (Debug.verboseOn())
            Debug.logVerbose("[WfProcess.start] : Started the workflow process.", module);

        // set the actualStartDate
        try {
            GenericValue v = getRuntimeObject();
            v.set("actualStartDate", UtilDateTime.nowTimestamp());
            v.store();
        } catch (GenericEntityException e) {
            Debug.logWarning("Could not set 'actualStartDate'.", module);
            e.printStackTrace();
        }
        startActivity(start);
    }

    /**
     * @see org.ofbiz.workflow.WfProcess#manager()
     */
    public WfProcessMgr manager() throws WfException {
        return manager;
    }

    /**
     * @see org.ofbiz.workflow.WfProcess#requester()
     */
    public WfRequester requester() throws WfException {
        return requester;
    }

    /**
     * @see org.ofbiz.workflow.WfProcess#getIteratorStep()
     */
    public Iterator<WfActivity> getIteratorStep() throws WfException {
        return activeSteps().iterator();
    }

    /**
     * @see org.ofbiz.workflow.WfProcess#isMemberOfStep(org.ofbiz.workflow.WfActivity)
     */
    public boolean isMemberOfStep(WfActivity member) throws WfException {
        return activeSteps().contains(member);
    }

    /**
     * @see org.ofbiz.workflow.WfProcess#getActivitiesInState(java.lang.String)
     */
    public Iterator<WfActivity> getActivitiesInState(String state) throws WfException, InvalidState {
        List<WfActivity> res = new ArrayList<WfActivity>();
        
        for (WfActivity a : activeSteps()) {
            if (a.state().equals(state))
                res.add(a);
        }
        return res.iterator();
    }

    /**
     * @see org.ofbiz.workflow.WfProcess#result()
     */
    public Map<String, Object> result() throws WfException, ResultNotAvailable {
        Map<String, Object> resultSig = manager().resultSignature();
        Map<String, Object> results = new HashMap<String, Object>();
        Map<String, Object> context = processContext();

        if (resultSig != null) {
            Set<String> resultKeys = resultSig.keySet();
            
            for (String key : resultKeys) {
                if (context.containsKey(key))
                    results.put(key, context.get(key));
            }
        }
        return results;
    }

    /**
     * @see org.ofbiz.workflow.WfProcess#howManyStep()
     */
    public int howManyStep() throws WfException {
        return activeSteps().size();
    }

    /**
     * @see org.ofbiz.workflow.WfProcess#receiveResults(org.ofbiz.workflow.WfActivity, java.util.Map)
     */
    public synchronized void receiveResults(WfActivity activity, Map<String, Object> results) throws WfException, InvalidData {
        Map<String, Object> context = processContext();
        context.putAll(results);
        setSerializedData(context);
    }

    /**
     * @see org.ofbiz.workflow.WfProcess#activityComplete(org.ofbiz.workflow.WfActivity)
     */
    public synchronized void activityComplete(WfActivity activity) throws WfException {
        if (!activity.state().equals("closed.completed"))
            throw new WfException("Activity state is not completed");
        if (Debug.verboseOn()) Debug.logVerbose("[WfProcess.activityComplete] : Activity (" + activity.name() + ") is complete", module);
        queueNext(activity);
    }

    /**
     * @see org.ofbiz.workflow.impl.WfExecutionObjectImpl#executionObjectType()
     */
    @Override
    public String executionObjectType() {
        return "WfProcess";
    }

    // Queues the next activities for processing
    private void queueNext(WfActivity fromActivity) throws WfException {
        List<GenericValue> nextTrans = getTransFrom(fromActivity);

        if (nextTrans.size() > 0) {
            for (GenericValue trans : nextTrans) {
                // Get the activity definition
                GenericValue toActivity = null;

                try {
                    toActivity = trans.getRelatedOne("ToWorkflowActivity");
                } catch (GenericEntityException e) {
                    throw new WfException(e.getMessage(), e);
                }

                // check for a join
                String join = "WJT_AND"; // default join is AND

                if (toActivity.get("joinTypeEnumId") != null)
                    join = toActivity.getString("joinTypeEnumId");

                if (Debug.verboseOn()) Debug.logVerbose("[WfProcess.queueNext] : " + join + " join.", module);

                // activate if XOR or test the join transition(s)
                if (join.equals("WJT_XOR"))
                    startActivity(toActivity);
                else
                    joinTransition(toActivity, trans);
            }
        } else {
            if (Debug.verboseOn())
                Debug.logVerbose("[WfProcess.queueNext] : No transitions left to follow.", module);
            this.finishProcess();
        }
    }

    // Follows the and-join transition
    private void joinTransition(GenericValue toActivity,
        GenericValue transition) throws WfException {
        // get all TO transitions to this activity
        GenericValue dataObject = getRuntimeObject();
        
        List<GenericValue> toTrans = null;
        try {
            toTrans = toActivity.getRelated("ToWorkflowTransition");
        } catch (GenericEntityException e) {
            throw new WfException(e.getMessage(), e);
        }

        // get a list of followed transition to this activity
        List<GenericValue> followed = null;
        try {
            Map<String, Object> fields = new HashMap<String, Object>();
            fields.put("processWorkEffortId", dataObject.getString("workEffortId"));
            fields.put("toActivityId", toActivity.getString("activityId"));
            followed = getDelegator().findByAnd("WorkEffortTransBox", fields);
        } catch (GenericEntityException e) {
            throw new WfException(e.getMessage(), e);
        }

        if (Debug.verboseOn()) Debug.logVerbose("[WfProcess.joinTransition] : toTrans (" + toTrans.size() + ") followed (" +
                (followed.size() + 1) + ")", module);

        // check to see if all transition requirements are met
        if (toTrans.size() == (followed.size() + 1)) {
            Debug.logVerbose("[WfProcess.joinTransition] : All transitions have followed.", module);
            startActivity(toActivity);
            try {
                Map<String, Object> fields = new HashMap<String, Object>();
                fields.put("processWorkEffortId", dataObject.getString("workEffortId"));
                fields.put("toActivityId", toActivity.getString("activityId"));
                getDelegator().removeByAnd("WorkEffortTransBox", fields);
            } catch (GenericEntityException e) {
                throw new WfException(e.getMessage(), e);
            }
        } else {
            Debug.logVerbose("[WfProcess.joinTransition] : Waiting for transitions to finish.", module);
            try {
                Map<String, Object> fields = new HashMap<String, Object>();
                fields.put("processWorkEffortId", dataObject.getString("workEffortId"));
                fields.put("toActivityId", toActivity.getString("activityId"));
                fields.put("transitionId", transition.getString("transitionId"));
                GenericValue obj = getDelegator().makeValue("WorkEffortTransBox", fields);

                getDelegator().create(obj);
            } catch (GenericEntityException e) {
                throw new WfException(e.getMessage(), e);
            }
        }
    }

    // Activates an activity object
    private void startActivity(GenericValue value) throws WfException {
        WfActivity activity = WfFactory.getWfActivity(value, workEffortId);
        GenericResultWaiter req = new GenericResultWaiter();

        if (Debug.verboseOn()) Debug.logVerbose("[WfProcess.startActivity] : Attempting to start activity (" + activity.name() + ")", module);

        // locate the dispatcher to use
        LocalDispatcher dispatcher = this.getDispatcher();

        // get the job manager
        JobManager jm = dispatcher.getJobManager();
        if (jm == null) {
            throw new WfException("No job manager found on the service dispatcher; cannot start activity");
        }

        // using the StartActivityJob class to run the activity within its own thread
        try {
            Job activityJob = new StartActivityJob(activity, req);
            jm.runJob(activityJob);
        } catch (JobManagerException e) {
            throw new WfException("JobManager error", e);
        }

        // the GenericRequester object will hold any exceptions; and report the job as failed
        if (req.status() == GenericResultWaiter.SERVICE_FAILED) {
            Throwable reqt = req.getThrowable();
            if (reqt instanceof CannotStart)
                Debug.logVerbose("[WfProcess.startActivity] : Cannot start activity. Waiting for manual start.", module);
            else if (reqt instanceof AlreadyRunning)
                throw new WfException("Activity already running", reqt);
            else
                throw new WfException("Activity error", reqt);
        }
    }

    // Determine the next activity or activities
    private List<GenericValue> getTransFrom(WfActivity fromActivity) throws WfException {
        List<GenericValue> transList = new ArrayList<GenericValue>();
        
        // get the from transitions
        List<GenericValue> fromTransitions = null;
        try {
            fromTransitions = fromActivity.getDefinitionObject().getRelated("FromWorkflowTransition");
        } catch (GenericEntityException e) {
            throw new WfException(e.getMessage(), e);
        }

        // check for a split
        String split = "WST_AND"; // default split is AND

        if (fromActivity.getDefinitionObject().get("splitTypeEnumId") != null)
            split = fromActivity.getDefinitionObject().getString("splitTypeEnumId");

        // the default value is TRUE, so if no expression is supplied we evaluate as true
        boolean transitionOk = true;

        // the otherwise condition (only used by XOR splits)
        GenericValue otherwise = null;

        // iterate through the possible transitions
        for (GenericValue transition : fromTransitions) {
            // if this transition is OTHERWISE store it for later and continue on
            if (transition.get("conditionTypeEnumId") != null && transition.getString("conditionTypeEnumId").equals("WTC_OTHERWISE")) {
                // there should be only one of these, if there is more then one we will use the last one defined
                otherwise = transition;
                continue;
            }

            // get the condition body from the condition tag
            String conditionBody = transition.getString("conditionExpr");

            // get the extended attributes for the transition
            Map<String, String> extendedAttr = StringUtil.strToMap(transition.getString("extendedAttributes"));

            // check for a conditionClassName attribute if exists use it
            if (extendedAttr != null && extendedAttr.get("conditionClassName") != null) {
                String conditionClassName = extendedAttr.get("conditionClassName");
                transitionOk = this.evalConditionClass(conditionClassName, conditionBody, this.processContext(), extendedAttr);
            } else {
                // since no condition class is supplied, evaluate the expression using bsh
                if (conditionBody != null) {
                    transitionOk = this.evalBshCondition(conditionBody, this.processContext());
                }
            }

            if (transitionOk) {
                transList.add(transition);
                if (split.equals("WST_XOR"))
                    break;
            }
        }

        // we only use the otherwise transition for XOR splits
        if (split.equals("WST_XOR") && transList.size() == 0 && otherwise != null) {
            transList.add(otherwise);
            Debug.logVerbose("Used OTHERWISE Transition.", module);
        }

        if (Debug.verboseOn()) Debug.logVerbose("[WfProcess.getTransFrom] : Transitions: " + transList.size(), module);
        return transList;
    }

    // Complete this workflow
    private void finishProcess() throws WfException {
        changeState("closed.completed");
        Debug.logVerbose("[WfProcess.finishProcess] : Workflow Complete. Calling back to requester.", module);
        if (requester != null) {
            WfEventAudit audit = WfFactory.getWfEventAudit(this, null); // this will need to be updated

            try {
                requester.receiveEvent(audit);
            } catch (InvalidPerformer e) {
                throw new WfException(e.getMessage(), e);
            }
        }
    }

    // Get the active process activities
    private List<WfActivity> activeSteps() throws WfException {
        List<WfActivity> steps = new ArrayList<WfActivity>();

        List<GenericValue> workEffortList = null;
        try {
            workEffortList = getDelegator().findByAnd("WorkEffort", UtilMisc.toMap("workEffortParentId", runtimeKey()));
        } catch (GenericEntityException e) {
            throw new WfException(e.getMessage(), e);
        }
        if (workEffortList == null) {
            return steps;
        }
        
        for (GenericValue workEffort : workEffortList) {
            if (workEffort.get("currentStatusId") != null &&
                WfUtil.getOMGStatus(workEffort.getString("currentStatusId")).startsWith("open."))
                steps.add(WfFactory.getWfActivity(getDelegator(), workEffort.getString("workEffortId")));
        }
        return steps;
    }
}

