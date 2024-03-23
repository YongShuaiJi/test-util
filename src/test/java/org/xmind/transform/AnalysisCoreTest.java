package org.xmind.transform;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;
import org.xmind.transform.dto.TreeNode;
import org.xmind.transform.dto.XMind;
import org.xmind.transform.execute.AnalysisCore;
import org.xmind.transform.dto.XMindStep;

import java.util.ArrayList;
import java.util.List;


/**
 * @author jiyongshuai
 * @email jysnana@163.com
 * @Date 2024-03-22 00:15:28
 * AnalysisCore unit test case
 */
public class AnalysisCoreTest {

    /**
     * 验证最少的节点标准输入标题、操作步骤、预期结果
     * */
    @Test
    public void convertToBaseActionTestCorrectChildren() {
        AnalysisCore analysisCore = new AnalysisCore();

        // 创建第一层XMind对象和节点
        XMind xmind = Mockito.mock(XMind.class);
        Mockito.when(xmind.getTitle()).thenReturn("caseTitle");
        TreeNode xMindNode = Mockito.mock(TreeNode.class);

        // 创建第二层XMind对象
        XMind step  = Mockito.mock(XMind.class);
        Mockito.when(step.getTitle()).thenReturn("Step");
        TreeNode stepChildren = Mockito.mock(TreeNode.class);

        // 创建第三层XMind对象
        XMind expectedResult = Mockito.mock(XMind.class);
        Mockito.when(expectedResult.getTitle()).thenReturn("ExpectedResult");

        // 设置第一个节点的调用结果
        List<XMind> xMindList = new ArrayList<>();
        xMindList.add(step);

        Mockito.when(xmind.getChildren()).thenReturn(xMindNode);
        Mockito.when(xMindNode.getAttached()).thenReturn(xMindList);

        // 设置第二个节点的调用结果
        List<XMind> stepList = new ArrayList<>();
        stepList.add(expectedResult);

        Mockito.when(step.getChildren()).thenReturn(stepChildren);
        Mockito.when(stepChildren.getAttached()).thenReturn(stepList);

        // 设置第三个节点的调用结果，expectedResult是最后一层节点，往下没有节点了
        Mockito.when(expectedResult.getChildren()).thenReturn(null);

        // 调用实际代码运行
        List<XMindStep> steps = analysisCore.convertToBaseAction(xmind, new ArrayList<>(), new StringBuilder());
        // 结果校验
        Assertions.assertEquals(1, steps.size());
        XMindStep step1 = steps.get(0);
        // 验证用例标题，操作步骤和预期结果的值
        Assertions.assertEquals("caseTitle", step1.getTitle());
        Assertions.assertEquals("Step", step1.getStep());
        Assertions.assertEquals("ExpectedResult", step1.getExpectedResult());
    }

    @Test
    public void TestConvertToBaseActionNoChildren() {
        AnalysisCore analysisCore = new AnalysisCore();

        // 第一层节点
        XMind xmind = Mockito.mock(XMind.class);
        TreeNode node = Mockito.mock(TreeNode.class);
        Mockito.when(xmind.getChildren()).thenReturn(node);
        Mockito.when(xmind.getTitle()).thenReturn("Title");
        // 第二层节点
        XMind child = Mockito.mock(XMind.class);
        Mockito.when(child.getChildren()).thenReturn(null);
        // 第二层节点调用设置
        List<XMind> xmindStepList = new ArrayList<>();
        xmindStepList.add(child);
        Mockito.when(node.getAttached()).thenReturn(xmindStepList);
        // 校验节点不足的场景下会抛出希望的异常
        Assertions.assertThrows(RuntimeException.class, () -> {
            analysisCore.convertToBaseAction(xmind, new ArrayList<>(), new StringBuilder());
        });
    }
}