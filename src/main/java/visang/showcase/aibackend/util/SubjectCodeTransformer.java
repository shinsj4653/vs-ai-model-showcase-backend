package visang.showcase.aibackend.util;


import java.util.HashMap;

public class SubjectCodeTransformer {

    private static final HashMap<String, String> transformationMap = new HashMap<>();

    static {
        transformationMap.put("T1011", "초1-1");
        transformationMap.put("T1012", "초1-2");
        transformationMap.put("T1013", "초2-1");
        transformationMap.put("T1014", "초2-2");
        transformationMap.put("T1015", "초3-1");
        transformationMap.put("T1016", "초3-2");
        transformationMap.put("T1017", "초4-1");
        transformationMap.put("T1018", "초4-2");
        transformationMap.put("T1019", "초5-1");
        transformationMap.put("T101A", "초5-2");
        transformationMap.put("T101B", "초6-1");
        transformationMap.put("T101C", "초6-2");
        transformationMap.put("T2011", "중1-1");
        transformationMap.put("T2012", "중1-2");
        transformationMap.put("T2013", "중2-1");
        transformationMap.put("T2014", "중2-2");
        transformationMap.put("T2015", "중3-1");
        transformationMap.put("T2016", "중3-2");
        transformationMap.put("T3011", "고1-1");
        transformationMap.put("T3012", "고1-2");
        transformationMap.put("T3013", "수학I");
        transformationMap.put("T3014", "수학II");
        transformationMap.put("T3015", "미적분");
        transformationMap.put("T3016", "확률과 통계");
        transformationMap.put("T3017", "기하");

    }

    public static String transform(String subjectCode) {
        return transformationMap.getOrDefault(subjectCode, "Unknown Subject Code");
    }

}
