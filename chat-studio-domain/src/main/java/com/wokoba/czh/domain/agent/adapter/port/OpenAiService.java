package com.wokoba.czh.domain.agent.adapter.port;

import java.util.List;

public interface OpenAiService {

    List<String> modelList(String completionsUrl, String apiKey);
}
