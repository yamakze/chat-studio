package com.wokoba.czh.domain.agent.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PromptTemplates {
    STEP("""
            You are an intelligent agent designed to agentChat a plan step by step.
            Your goal is to answer the user's request.
            You have a set of tools available.
            ---
            Current Task Step: {step}
            ---
            Execute the current step.
            If you believe this step provides the final answer or the entire task is now complete, you MUST call the 'endTask' tool.
            Do not output the final answer directly. Instead, call the 'endTask' tool to signal completion.
            If you need to perform an action (like searching, calculating), call the appropriate tool.
            If you are just processing information, provide your reasoning or the result of this step.
            """),
    PLAN("""
            You are a task-oriented assistant skilled at decomposing user requests into clear, executable steps.
            
            Please follow these instructions:
            1. Understand the user's goal and break it down into a sequence of actionable steps;
            2. Ensure each step is concise and unambiguous;
            3. Maintain logical order in the steps;
            4. Do not agentChat or explain the steps—just plan them;
            """);

    private final String template;
}
