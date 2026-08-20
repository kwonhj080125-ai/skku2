package com.example.skku2_backend.feedback.gpt;

import com.example.skku2_backend.dailyrecord.dto.CorrelationResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

/**
 * 파이프라인 3단계: CorrelationAnalysisService가 계산한 결과를 GPT 프롬프트로 조립한다.
 * GPT는 여기 담긴 occurrence/total/ratio 숫자를 절대 새로 만들면 안 되고,
 * 그대로 인용해서 문장으로 바꾸는 역할만 하도록 System Prompt에서 강하게 규칙을 건다.
 */
@Component
@RequiredArgsConstructor
public class GptPromptBuilder {

    private final ObjectMapper objectMapper;

    public static final String SYSTEM_PROMPT = """
            당신은 스킨케어 앱 'TRACE'의 피부-웰니스 상관관계 피드백 작성자입니다.

            [역할]
            백엔드에서 이미 계산된 "이벤트-반응 상관관계 데이터"를 받아서,
            사용자가 이해하기 쉬운 한 문장의 인사이트로 바꾸는 것이 유일한 임무입니다.

            [절대 규칙]
            1. 입력으로 주어지지 않은 숫자, 비율, 횟수를 절대로 새로 만들어내지 마세요.
               반드시 입력 데이터에 있는 occurrence, total, ratio 값만 그대로 인용하세요.
            2. "원인이다", "때문이다"라고 인과관계를 단정하지 마세요.
               대신 "~이어진 다음이었어요", "~한 경우가 많았어요"처럼
               관찰된 패턴(상관관계)으로만 표현하세요.
            3. 의학적 진단, 질환명, 치료 조언을 하지 마세요.
            4. 담담하고 담백한 어조를 유지하세요.
            5. total < 3 이거나 ratio < 0.5 인 패턴은 "기록이 더 필요하다"고 정직하게 답하세요.
            6. 특정 브랜드를 비방하거나 단정적으로 탓하지 마세요.

            [출력 형식]
            반드시 아래 JSON 스키마로만 응답하세요. 다른 텍스트를 덧붙이지 마세요.
            {
              "headline": "가장 강한 패턴 1개를 담은 한 문장 (40자 내외, 해요체)",
              "supporting_stat": "N번 중 M번 형태의 근거 문장 (없으면 null)",
              "confidence": "high | medium | low",
              "secondary_patterns": ["보조 패턴 문장1", "보조 패턴 문장2"],
              "insufficient_data_note": "기록 부족 안내 문구 (해당 없으면 null)"
            }
            """;

    public String buildUserPrompt(Long userId, int periodDays, List<CorrelationResult> correlationData) {
        String correlationJson = toJson(correlationData);

        return """
                다음은 사용자 [user_id: %d]의 최근 %d일간
                피부-웰니스 데이터에서 계산된 상관관계 결과입니다.

                [계산된 상관관계 데이터] (백엔드에서 미리 산출, 숫자 그대로 사용할 것)
                %s

                위 데이터를 바탕으로 System Prompt의 규칙과 JSON 스키마에 따라
                사용자에게 보여줄 피드백을 작성하세요.
                가장 ratio가 높고 total(표본 수)이 충분한(3회 이상) 패턴을 headline으로 우선 채택하세요.
                만약 위 데이터가 비어있다면, insufficient_data_note만 채우고
                headline은 "아직 패턴을 판단하기엔 기록이 더 필요해요" 계열로 작성하세요.
                """.formatted(userId, periodDays, correlationJson);
    }

    private String toJson(List<CorrelationResult> correlationData) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(correlationData);
        } catch (Exception e) {
            throw new IllegalStateException("상관관계 데이터 JSON 직렬화 실패", e);
        }
    }
}