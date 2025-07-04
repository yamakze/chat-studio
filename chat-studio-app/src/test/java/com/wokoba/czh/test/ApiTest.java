package com.wokoba.czh.test;

import com.wokoba.czh.domain.agent.model.entity.AiChatRequestEntity;
import com.wokoba.czh.domain.agent.service.IAiAgentService;
import com.wokoba.czh.domain.agent.service.chat.AiChatService;
import com.wokoba.czh.domain.agent.service.memory.analyzer.MemoryConsolidationService;
import com.wokoba.czh.domain.agent.service.memory.reflector.MemoryReflectionService;
import com.wokoba.czh.trigger.job.MemoryReflectorJob;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Flux;

import java.util.List;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class ApiTest {
    @Resource
    private AiChatService aiChatService;

    @Resource
    private IAiAgentService agentService;

    @Autowired
    private MemoryConsolidationService memoryConsolidationService;
    @Autowired
    private MemoryReflectorJob memoryReflectorJob;

    @Test
    public void test_01() {
        String 鸡你太美 = aiChatService.aiChat(new AiChatRequestEntity().setClientId(5L).setUserMessage("鸡你太美")).getResult().getOutput().getText();

        String 将你刚才的回复翻译过来 = aiChatService.aiChat(new AiChatRequestEntity().setClientId(5L).setUserMessage("将你刚才的回复翻译过来")).getResult().getOutput().getText();
        log.info("回复1:{}", 鸡你太美);
        log.info("回复2:{}", 将你刚才的回复翻译过来);
        log.info("测试完成");
    }

    @Test
    public void test_02() {
        List<String> answerList = aiChatService.aiChatStream(new AiChatRequestEntity()
                        .setUserMessage("检索出新增设备接口的相关信息")
                        .setClientId(5L)
                        .setRagId(1L))
                .map(chatResponse -> chatResponse.getResult().getOutput().getText())
                .collectList().block();

        log.info("回复:{}", answerList);
//        String response = aiChatService.aiChat(5L, "将你刚才的回答记录为.txt存放在/Users/chenzihao/Desktop/");
        Flux<ChatResponse> chatResponseFlux = aiChatService.aiChatStream(new AiChatRequestEntity()
                .setUserMessage("将你刚才的回答记录为.txt存放在/Users/chenzihao/Desktop/")
                .setClientId(5L)
                .setRagId(1L));
        List<String> response = chatResponseFlux.map(chatResponse -> chatResponse.getResult().getOutput().getText())
                .collectList().block();

        log.info("回复2:{}", response);
        log.info("测试完成");
    }

    @Test
    public void test_03() {
        List<String> response = aiChatService.aiChatStream(new AiChatRequestEntity()
                        .setRagId(1L)
                        .setClientId(5L)
                        .setUserMessage("/Users/chenzihao/Desktop下有哪些文件？"))
                .mapNotNull(chatResponse -> chatResponse.getResult().getOutput().getText())
                .collectList().block();
//        String response = aiChatService.aiChat(5L, 1L,"/Users/chenzihao/Desktop下有哪些文件？");
        log.info("response:{} ", response);
        log.info("测试完成");
    }

    @Test
    public void test_04() {

        List<String> response = aiChatService.aiChatStream(new AiChatRequestEntity()
                        .setClientId(5L)
                        .setUserMessage("将「- **请求方式, **：`POST, `\n" +
                                "- **请求, 地址**：`, localhost:80, 99/api/v, 1/equipment`, \n" +
                                "- **角色要求, **：企业管理员, （`Constants.Role, Key.EmployeeKey., EMPLOYEE_ADMIN`, ）\n" +
                                "\n" +
                                "### 请求参数, \n" +
                                "| 参数,                  | 类型,            | 是否必填,    | 说明,       |\n" +
                                "|--------------------, |--------------|--------, |---------|\n" +
                                "|,  registrationCode   |,  String       | 是,       | 登记, 编码    |\n" +
                                "|,  factoryNumber      |,  String       |,  否      |,  工厂编号,     |\n" +
                                "| typeId,              | String       |,  否      |,  设备类型ID,   |\n" +
                                "| responsible, EmpList | List, <String> | 是,       |,  责任人账号, 列表 |\n" +
                                "| model,               | String,        | 是      |,  设备型号,     |\n" +
                                "| manufacturers,       | String       |,  是      |,  制造商,      |\n" +
                                "| outBus, inum         |,  String       |,  否      |,  外部业务编号,   |\n" +
                                "| name,                | String,        | 是      |,  设备名称,     |\n" +
                                "| manufacturingDate,   | String,        | 否,       | 制造, 日期    |\n" +
                                "|,  verifyReportCode,    | String       |,  否      |,  验证报告, 编码  |\n" +
                                "|,  useRegistration    |,  String       |,  否      | 使用, 登记号   |, \n" +
                                "| address            |,  String       | 是,       | 设备, 使用地址  |, \n" +
                                "\n" +
                                "### 请求示例, \n" +
                                "```json, \n" +
                                "{\n" +
                                "  \"registration, Code\": \"REG, 2023, 001\",, \n" +
                                "  \"factoryNumber, \": \"F1, 2345, \",\n" +
                                "  \"type, Id\": \"TYPE, 001\",, \n" +
                                "  \"responsible, EmpList\": [\", 张三\", \", 李四\"],, \n" +
                                "  \"model\": \", YX-5, 000\",, \n" +
                                "  \"manufacturers, \": \"XX重工, \",\n" +
                                "  \"out, Businum\":,  \"BUSI0, 01\",, \n" +
                                "  \"name\": \", 压力容器-A1, \",\n" +
                                "  \"manufact, uringDate\": \", 2022, -08-, 01\",, \n" +
                                "  \"verifyReportCode, \": \"VER1, 2345, 6\",\n" +
                                "  \", useRegistration\": \", USE001, \",\n" +
                                "  \"address, \": \"上海市浦东, 新区张江高科技, 园区\"\n" +
                                "}」这段内容以.txt的形式存放在/Users/chenzihao/Desktop/"))
                .map(chatResponse -> chatResponse.getResult().getOutput().getText())
                .collectList().block();


        log.info("response:{} ", response);
        log.info("测试完成");
    }


    @Test
    public void test_05() {
        String response = agentService.agentChat(43L,"编写一部现代白领都市女性的日记");
        log.info("agentChat 测试结果:{} ", response);
        log.info("测试完成");
    }
    @Test
    public void test_06() {
        memoryConsolidationService.analyzeAndStoreMemories(0L,List.of());

        log.info("测试完成");
    }

    @Test
    public void test_07(){
        log.info("测试开始");
        memoryReflectorJob.reflectOnRecentMemories();
        log.info("测试完成");
    }


}
