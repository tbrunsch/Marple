package com.AMS.jBEAM.javaParser.utils.dataProviders;

import com.AMS.jBEAM.javaParser.result.CompletionSuggestionIF;
import com.AMS.jBEAM.javaParser.result.CompletionSuggestionObjectTreeNode;
import com.AMS.jBEAM.javaParser.result.CompletionSuggestions;
import com.AMS.jBEAM.javaParser.settings.ObjectTreeNodeIF;
import com.AMS.jBEAM.javaParser.utils.ParseUtils;

import java.util.List;
import java.util.Map;
import java.util.function.ToIntFunction;

public class ObjectTreeNodeDataProvider
{
	public CompletionSuggestions suggestNodes(String expectedName, ObjectTreeNodeIF contextNode, int insertionBegin, int insertionEnd) {
		List<ObjectTreeNodeIF> nodes = contextNode.getChildNodes();
		Map<CompletionSuggestionIF, Integer> ratedSuggestions = ParseUtils.createRatedSuggestions(
			nodes,
			node -> new CompletionSuggestionObjectTreeNode(node, insertionBegin, insertionEnd),
			rateNodeByNameFunc(expectedName)
		);
		return new CompletionSuggestions(insertionBegin, ratedSuggestions);
	}

	private int rateNodeByName(ObjectTreeNodeIF node, String expectedName) {
		return ParseUtils.rateStringMatch(node.getName(), expectedName);
	}

	private ToIntFunction<ObjectTreeNodeIF> rateNodeByNameFunc(String expectedName) {
		return node -> rateNodeByName(node, expectedName);
	}
}
