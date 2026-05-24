package es.upm.smarthome;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.core.AID;
import java.util.Arrays;

public abstract class AgentBase extends Agent {
    protected AgentModel type;
    protected String[] params;

    protected void setup() {
        super.setup();
        if (getArguments() != null) {
            this.params = Arrays.asList(getArguments()).toArray(new String[getArguments().length]);
        }
    }

    public DFAgentDescription[] getAgentsDF(AgentModel type) {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription templateSd = new ServiceDescription();
        templateSd.setType(type.getValue());
        template.addServices(templateSd);
        DFAgentDescription[] result = new DFAgentDescription[0];
        try {
            result = DFService.search(this, template);
        } catch (FIPAException e) {
            e.printStackTrace();
            loge("DFService.search failed");
        }
        return result;
    }

    public void registerAgentDF() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType(this.type.getValue());
        sd.setName(getLocalName());
        dfd.addServices(sd);
        try {
            DFAgentDescription[] results = DFService.search(this, dfd);
            if (results == null || results.length == 0) {
                DFService.register(this, dfd);
                log("Registered in DF as " + this.type.getValue());
            }
        } catch (FIPAException e) {
            loge("Unable to register in DF");
            doDelete();
        }
    }

    public void deregisterAgentDF() {
        try {
            DFService.deregister(this);
        } catch (FIPAException e) {
            loge("Unable to deregister from DF");
            doDelete();
        }
    }

    @Override
    public void doDelete() {
        deregisterAgentDF();
        super.doDelete();
        loge("Agent deleted");
    }

    public void log(String s) {
        System.out.println(System.currentTimeMillis() + ": " + getLocalName() + " - " + s);
    }

    public void loge(String s) {
        System.err.println(System.currentTimeMillis() + ": " + getLocalName() + " - " + s);
    }
}