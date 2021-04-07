package com.speech.ailotok;

import com.speech.ailotok.model.FlowNode;

import java.util.Vector;

public class FlowParser {

    public static Vector<FlowNode> createFlow(String userName, String flowString) {
        Vector<FlowNode> flow = new Vector<>();
        int dollarCounter = 0;
        StringBuilder letters = new StringBuilder();
        FlowNode flowNode = new FlowNode();
        for (char letter : flowString.toCharArray()) {
            if (letter == '$') {
                String phrase = letters.length() > 0 ? letters.toString() : null;
                dollarCounter++;
                if (dollarCounter == 2) {
                    flowNode.setQuestion(phrase);
                } else if (dollarCounter == 3) {
                    flowNode.setNegativeResponse(phrase);
                } else if (dollarCounter == 4) {
                    flowNode.setPositiveResponse(phrase);
                    flow.add(flowNode);
                    flowNode = new FlowNode();
                    dollarCounter = 1;
                }
                letters = new StringBuilder();
            } else if (letter == '*')
                letters.append(userName);
            else
                letters.append(letter);
        }
        return flow;
    }
}
