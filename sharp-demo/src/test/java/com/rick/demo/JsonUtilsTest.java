package com.rick.demo;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.rick.common.util.JsonUtils;
import com.rick.demo.module.project.domain.entity.Dept;
import com.rick.demo.module.project.domain.enums.TestCodeEnum;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Rick
 * @createdAt 2021-06-02 19:13:00
 */
public class JsonUtilsTest {

    @Test
    public void testListList() {
        List<List<String>> list = new ArrayList<>();
        list.add(Arrays.asList("1", "2"));
        list.add(Arrays.asList("3", "4"));

        System.out.println(JsonUtils.toJson(list));
        System.out.println(JsonUtils.toList("[[\"1\",\"2\"],[\"3\",\"4\"]]", List.class));
        System.out.println(JsonUtils.toList("[[\"足球\", \"篮球\"]]", List.class));
    }

    @Test
    public void testToString() {
        // hello => "hello" => hello
        String hello = JsonUtils.toJson("hello");
        System.out.println(hello);

        String string = JsonUtils.toObject(hello, String.class);
        System.out.println(string);
    }

    @Test
    public void testToJson() {
        Dept dept = new Dept();
        dept.setId(1L);
        dept.setName("Dev");
        dept.setParentId(2L);
        String json = JsonUtils.toJson(dept);
        System.out.println(json);
    }

    @Test
    public void testToPOJO() {
        Dept dept = JsonUtils.toObject("{\"id\":1,\"name\":\"Dev\",\"parentId\":2}", Dept.class);
        assertThat(dept.getId().longValue()).isEqualTo(1L);
    }

    @Test
    public void testToMap() {
        Map dept = JsonUtils.toObject("{\"id\":1,\"name\":\"Dev\",\"parentId\":2}", Map.class);
        // dept.get("id") Integer类型
        assertThat(dept.get("id")).isEqualTo(1);
    }

    @Test
    public void testToList() {
        List list = JsonUtils.toObject("[{\"id\":1,\"name\":\"Dev\",\"parentId\":2}]", List.class);
        // dept.get("id") Integer类型
        assertThat(((Map)list.get(0)).get("id")).isEqualTo(1);

    }

    @Test
    public void testToListWithGenerics1() {
        TypeReference<List<Dept>> typeRef = new TypeReference<List<Dept>>() {};
        List<Dept> list = JsonUtils.toObject("[{\"id\":1,\"name\":\"Dev\",\"parentId\":2}]", typeRef);
        assertThat(list.get(0).getId().longValue()).isEqualTo(1L);
        assertThat(list.get(0).getName()).isEqualTo("Dev");
        assertThat(list.get(0).getParentId()).isEqualTo(2L);
    }

    @Test
    public void testToListWithGenerics2() {
        List<Dept> list = JsonUtils.toList("[{\"id\":1,\"name\":\"Dev\",\"parentId\":2}]", Dept.class);
        assertThat(list.get(0).getId().longValue()).isEqualTo(1L);
    }

    @Test
    public void testListStringToJsonNode() {
        JsonNode jsonNode = JsonUtils.toJsonNode("[{\"id\":1,\"name\":\"Dev\",\"parentId\":2}]");
        assertThat(jsonNode.get(0).get("id").longValue()).isEqualTo(1L);
    }

    @Test
    public void testObjectStringToJsonNode() {
        JsonNode jsonNode = JsonUtils.toJsonNode("{\"id\":1,\"name\":\"Dev\",\"parentId\":2}");
        assertThat(jsonNode.get("id").longValue()).isEqualTo(1L);
    }

    @Test
    public void testObjectToJsonNode() {
        Dept dept = new Dept();
        dept.setId(1L);
        dept.setName("Dev");
        dept.setParentId(2L);
        JsonNode jsonNode = JsonUtils.toJsonNode(dept);
        assertThat(jsonNode.get("id").asLong()).isEqualTo(1L);
    }

    @Test
    public void testMapToJsonNode() {
        String id = "1";
        Map<String, String> params = new HashMap<>();
        params.put("id", id);
        JsonNode jsonNode = JsonUtils.toJsonNode(params);
        assertThat(jsonNode.get("id").asLong()).isEqualTo(1L);
    }

    @Test
    public void testCodeEnum() {
        List<TestCodeEnum> TestCodeList1 = JsonUtils.toList("[\"11\", \"10\"]", TestCodeEnum.class);
        System.out.println(TestCodeList1);

        List<TestCodeEnum> TestCodeList2 = JsonUtils.toList("[\"0\", \"1\"]", TestCodeEnum.class);
        System.out.println(TestCodeList2);

        // 有@JsonValue 下面执行报错
//        List<TestCodeEnum> TestCodeList3 = JsonUtils.toList("[\"UNKNOWN\", \"CODE\"]", TestCodeEnum.class);
//        System.out.println(TestCodeList3);
    }

    @Test
    public void testBeautifyJSON() {
        String json = JsonUtils.beautifyJSON("{\"id\":\"942130527958585344\",\"updateBy\":\"1\",\"code\":\"SO2025041106\",\"partnerId\":725459982719410176,\"contactPerson\":\"寇海利\",\"contactNumber\":\"18920308071\",\"contactMail\":\"\",\"sourceOrderNum\":\"\",\"orderDate\":[2025,4,11],\"status\":\"PRODUCING\",\"remark\":\"一体轮*15套+20只边盖\",\"itemList\":[{\"id\":\"942130527958585345\",\"updateBy\":\"1\",\"materialId\":728705375351607296,\"materialCode\":\"F20007\",\"specification\":\"48V14\\\"400W  五星轮\",\"quantity\":15,\"unit\":\"EA\",\"deliveryDate\":[2025,4,24],\"customerMaterialCode\":\"\",\"remark\":\"到家20只边盖\",\"produceOrderId\":942130527958585344,\"batchId\":830823028777930752,\"batchCode\":\"45993c6a3e382cad7b31cec1ea486ed2\",\"itemCategory\":\"PRODUCT\",\"classificationList\":[{\"materialId\":728705375351607296,\"classificationCode\":\"COLOR\"}],\"complete\":false,\"produceOrderCode\":\"SO2025041106\",\"itemList\":[{\"id\":\"942130528025694208\",\"updateBy\":\"1\",\"materialId\":749281411621834753,\"materialCode\":\"R00943\",\"quantity\":1,\"unit\":\"EA\",\"remark\":\"\",\"componentDetailId\":728710489063821312,\"batchId\":836906307511767040,\"batchCode\":\"45993c6a3e382cad7b31cec1ea486ed2\",\"classificationList\":[{\"materialId\":749281411621834753,\"classificationCode\":\"COLOR\"}],\"produceOrderItemId\":942130527958585345,\"materialSpecification\":\"\",\"characteristic\":\"\",\"materialName\":\"\",\"unitText\":\"\",\"materialText\":\"\"},{\"id\":\"942130528025694209\",\"updateBy\":\"1\",\"materialId\":746758514692968448,\"materialCode\":\"R00932\",\"quantity\":1,\"unit\":\"EA\",\"remark\":\"\",\"componentDetailId\":728710489063821313,\"classificationList\":[],\"produceOrderItemId\":942130527958585345,\"materialSpecification\":\"\",\"characteristic\":\"\",\"materialName\":\"\",\"unitText\":\"\",\"materialText\":\"\"},{\"id\":\"942130528025694210\",\"updateBy\":\"1\",\"materialId\":729584532361060352,\"materialCode\":\"R00234\",\"quantity\":1,\"unit\":\"EA\",\"remark\":\"\",\"componentDetailId\":725450923417034752,\"batchId\":943452356115550208,\"batchCode\":\"45993c6a3e382cad7b31cec1ea486ed2\",\"classificationList\":[{\"materialId\":729584532361060352,\"classificationCode\":\"COLOR\"}],\"produceOrderItemId\":942130527958585345,\"materialSpecification\":\"\",\"characteristic\":\"\",\"materialName\":\"\",\"unitText\":\"\",\"materialText\":\"\"},{\"id\":\"942130528025694211\",\"updateBy\":\"1\",\"materialId\":794910646360985600,\"materialCode\":\"R01118\",\"quantity\":1,\"unit\":\"EA\",\"remark\":\"\",\"componentDetailId\":725450923417034753,\"classificationList\":[],\"produceOrderItemId\":942130527958585345,\"materialSpecification\":\"\",\"characteristic\":\"\",\"materialName\":\"\",\"unitText\":\"\",\"materialText\":\"\"},{\"id\":\"942130528025694212\",\"updateBy\":\"1\",\"materialId\":729584900801306624,\"materialCode\":\"R00772\",\"quantity\":1,\"unit\":\"EA\",\"remark\":\"\",\"componentDetailId\":725450923417034754,\"classificationList\":[],\"produceOrderItemId\":942130527958585345,\"materialSpecification\":\"\",\"characteristic\":\"\",\"materialName\":\"\",\"unitText\":\"\",\"materialText\":\"\"},{\"id\":\"942130528025694213\",\"updateBy\":\"1\",\"materialId\":729584652565618688,\"materialCode\":\"R00316\",\"quantity\":1,\"unit\":\"EA\",\"remark\":\"加注油孔\",\"componentDetailId\":728715973766422528,\"batchId\":842714572409655296,\"batchCode\":\"45993c6a3e382cad7b31cec1ea486ed2\",\"classificationList\":[{\"materialId\":729584652565618688,\"classificationCode\":\"COLOR\"}],\"produceOrderItemId\":942130527958585345,\"materialSpecification\":\"\",\"characteristic\":\"\",\"materialName\":\"\",\"unitText\":\"\",\"materialText\":\"\"},{\"id\":\"942130528025694214\",\"updateBy\":\"1\",\"materialId\":794910552450519040,\"materialCode\":\"R01117\",\"quantity\":1,\"unit\":\"EA\",\"remark\":\"\",\"componentDetailId\":728715973766422529,\"classificationList\":[],\"produceOrderItemId\":942130527958585345,\"materialSpecification\":\"\",\"characteristic\":\"\",\"materialName\":\"\",\"unitText\":\"\",\"materialText\":\"\"},{\"id\":\"942130528025694215\",\"updateBy\":\"1\",\"materialId\":729584759725891584,\"materialCode\":\"R00505\",\"quantity\":1,\"unit\":\"EA\",\"remark\":\"\",\"componentDetailId\":725451196411699200,\"classificationList\":[],\"produceOrderItemId\":942130527958585345,\"materialSpecification\":\"\",\"characteristic\":\"\",\"materialName\":\"\",\"unitText\":\"\",\"materialText\":\"\"},{\"id\":\"942130528025694216\",\"updateBy\":\"1\",\"materialId\":729584669766459392,\"materialCode\":\"R00328\",\"quantity\":1,\"unit\":\"EA\",\"remark\":\"\",\"componentDetailId\":725451196411699201,\"classificationList\":[],\"produceOrderItemId\":942130527958585345,\"materialSpecification\":\"\",\"characteristic\":\"\",\"materialName\":\"\",\"unitText\":\"\",\"materialText\":\"\"},{\"id\":\"942130528025694217\",\"updateBy\":\"1\",\"materialId\":729584669766459392,\"materialCode\":\"R00328\",\"quantity\":1,\"unit\":\"EA\",\"remark\":\"\",\"componentDetailId\":725451196411699202,\"classificationList\":[],\"produceOrderItemId\":942130527958585345,\"materialSpecification\":\"\",\"characteristic\":\"\",\"materialName\":\"\",\"unitText\":\"\",\"materialText\":\"\"},{\"id\":\"942130528025694218\",\"updateBy\":\"1\",\"materialId\":729584748942336000,\"materialCode\":\"R00484\",\"quantity\":1,\"unit\":\"TAO\",\"remark\":\"\",\"componentDetailId\":735545413116923904,\"classificationList\":[],\"produceOrderItemId\":942130527958585345,\"materialSpecification\":\"\",\"characteristic\":\"\",\"materialName\":\"\",\"unitText\":\"\",\"materialText\":\"\"},{\"id\":\"942130528025694219\",\"updateBy\":\"1\",\"materialId\":748879516289847296,\"materialCode\":\"R00938\",\"quantity\":1,\"unit\":\"EA\",\"remark\":\"\",\"componentDetailId\":725452132173504512,\"classificationList\":[],\"produceOrderItemId\":942130527958585345,\"materialSpecification\":\"\",\"characteristic\":\"\",\"materialName\":\"\",\"unitText\":\"\",\"materialText\":\"\"},{\"id\":\"942130528025694220\",\"updateBy\":\"1\",\"materialId\":746758016619368448,\"materialCode\":\"R00930\",\"quantity\":1,\"unit\":\"EA\",\"remark\":\"\",\"componentDetailId\":725451860537794560,\"classificationList\":[],\"produceOrderItemId\":942130527958585345,\"materialSpecification\":\"\",\"characteristic\":\"\",\"materialName\":\"\",\"unitText\":\"\",\"materialText\":\"\"},{\"id\":\"942130528025694221\",\"updateBy\":\"1\",\"materialId\":729584669258948608,\"materialCode\":\"R00327\",\"quantity\":1,\"unit\":\"EA\",\"remark\":\"\",\"componentDetailId\":725451860537794561,\"classificationList\":[],\"produceOrderItemId\":942130527958585345,\"materialSpecification\":\"\",\"characteristic\":\"\",\"materialName\":\"\",\"unitText\":\"\",\"materialText\":\"\"},{\"id\":\"942130528025694222\",\"updateBy\":\"1\",\"materialId\":912713111725694976,\"materialCode\":\"R02168\",\"quantity\":1,\"unit\":\"GEN\",\"remark\":\"\",\"componentDetailId\":728709627578945536,\"classificationList\":[],\"produceOrderItemId\":942130527958585345,\"materialSpecification\":\"\",\"characteristic\":\"\",\"materialName\":\"\",\"unitText\":\"\",\"materialText\":\"\"},{\"id\":\"942130528025694225\",\"updateBy\":\"1\",\"materialId\":729584904882364416,\"materialCode\":\"R00780\",\"quantity\":1,\"unit\":\"KUAI\",\"remark\":\"\",\"componentDetailId\":728968962335166464,\"classificationList\":[],\"produceOrderItemId\":942130527958585345,\"materialSpecification\":\"\",\"characteristic\":\"\",\"materialName\":\"\",\"unitText\":\"\",\"materialText\":\"\"},{\"id\":\"942130528025694227\",\"updateBy\":\"1\",\"materialId\":729584774796025856,\"materialCode\":\"R00534\",\"quantity\":1,\"unit\":\"ZHII\",\"remark\":\"\",\"componentDetailId\":725452482737627136,\"classificationList\":[],\"produceOrderItemId\":942130527958585345,\"materialSpecification\":\"\",\"characteristic\":\"\",\"materialName\":\"\",\"unitText\":\"\",\"materialText\":\"\"},{\"id\":\"942130528025694228\",\"updateBy\":\"1\",\"materialId\":901106454785019904,\"materialCode\":\"R01440\",\"quantity\":0.15,\"unit\":\"KG\",\"remark\":\"8T\",\"componentDetailId\":725452482737627137,\"classificationList\":[],\"produceOrderItemId\":942130527958585345,\"materialSpecification\":\"\",\"characteristic\":\"\",\"materialName\":\"\",\"unitText\":\"\",\"materialText\":\"\"},{\"id\":\"942130528025694230\",\"updateBy\":\"1\",\"materialId\":729584695968276480,\"materialCode\":\"R00380\",\"quantity\":1,\"unit\":\"GEN\",\"remark\":\"567\",\"componentDetailId\":725452826834132992,\"classificationList\":[],\"produceOrderItemId\":942130527958585345,\"materialSpecification\":\"\",\"characteristic\":\"\",\"materialName\":\"\",\"unitText\":\"\",\"materialText\":\"\"},{\"id\":\"942130528025694231\",\"updateBy\":\"1\",\"materialId\":729584763890835456,\"materialCode\":\"R00513\",\"quantity\":1,\"unit\":\"EA\",\"remark\":\"\",\"componentDetailId\":728709397231964165,\"classificationList\":[],\"produceOrderItemId\":942130527958585345,\"materialSpecification\":\"\",\"characteristic\":\"\",\"materialName\":\"\",\"unitText\":\"\",\"materialText\":\"\"},{\"id\":\"942130528025694232\",\"updateBy\":\"1\",\"materialId\":729584318455750656,\"materialCode\":\"R00093\",\"quantity\":1,\"unit\":\"EA\",\"remark\":\"\",\"componentDetailId\":728709397231964166,\"batchId\":836906412373561344,\"batchCode\":\"45993c6a3e382cad7b31cec1ea486ed2\",\"classificationList\":[{\"materialId\":729584318455750656,\"classificationCode\":\"COLOR\"}],\"produceOrderItemId\":942130527958585345,\"materialSpecification\":\"\",\"characteristic\":\"\",\"materialName\":\"\",\"unitText\":\"\",\"materialText\":\"\"},{\"id\":\"942130528025694233\",\"updateBy\":\"1\",\"materialId\":729584789354455040,\"materialCode\":\"R00556\",\"quantity\":1,\"unit\":\"GEN\",\"remark\":\"\",\"componentDetailId\":728709397231964167,\"classificationList\":[],\"produceOrderItemId\":942130527958585345,\"materialSpecification\":\"\",\"characteristic\":\"\",\"materialName\":\"\",\"unitText\":\"\",\"materialText\":\"\"},{\"id\":\"942130528025694234\",\"updateBy\":\"1\",\"materialId\":764459407009763328,\"materialCode\":\"R01000\",\"quantity\":1,\"unit\":\"TAO\",\"remark\":\"盖母2 3毫米止转2 4垫片2 2垫片2 大圈1 前轴1\",\"componentDetailId\":764468564748259328,\"classificationList\":[],\"produceOrderItemId\":942130527958585345,\"materialSpecification\":\"\",\"characteristic\":\"\",\"materialName\":\"\",\"unitText\":\"\",\"materialText\":\"\"}],\"scheduleList\":[{\"id\":\"942131385827971072\",\"updateBy\":\"1\",\"code\":\"PP20250411014\",\"quantity\":15,\"unit\":\"EA\",\"unitText\":\"个\",\"status\":\"PLANNING\",\"remark\":\"\",\"produceOrderId\":942130527958585344,\"produceOrderCode\":\"SO2025041106\",\"produceOrderItemId\":942130527958585345}],\"contactInfo\":{\"id\":\"942130528487067648\",\"updateBy\":\"1\",\"contactSubject\":\"C00043-天津市小神牛科技发展有限公司\",\"contactPerson\":\"寇海利\",\"contactNumber\":\"18920308071\",\"contactMail\":\"\",\"address\":\"\",\"instanceId\":942130527958585345},\"attachmentList\":[],\"materialSpecification\":\"\",\"characteristic\":\"\",\"materialName\":\"\",\"unitText\":\"\",\"materialText\":\"\"}],\"attachmentList\":[],\"version\":18}");
        System.out.println(json);
    }

}
