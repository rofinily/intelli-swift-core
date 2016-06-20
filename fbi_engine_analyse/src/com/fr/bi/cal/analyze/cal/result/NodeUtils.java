package com.fr.bi.cal.analyze.cal.result;import com.fr.bi.stable.gvi.GVIFactory;import com.fr.bi.stable.gvi.GroupValueIndex;import com.fr.bi.stable.operation.sort.comp.ComparatorFacotry;import com.fr.bi.stable.report.key.TargetGettingKey;import com.fr.bi.stable.report.result.LightNode;import com.fr.bi.stable.structure.tree.NTree;/** * Created by Hiram on 2015/1/27. */public class NodeUtils {	    static Double getTopN(LightNode node, TargetGettingKey key, int N) {        Double nLine;        int count = node.getChildLength();        NTree<Double> tree = N < (count << 2) ? new NTree<Double>(ComparatorFacotry.DOUBLE_DESC, N) : new NTree<Double>(ComparatorFacotry.DOUBLE_ASC, count + 1 - N);        for (int i = 0; i < count; i++) {            LightNode child = node.getChild(i);            Number v = child.getSummaryValue(key);            if (v != null) {            	tree.add(v.doubleValue());            }        }        nLine = tree.getLineValue();        if(nLine == null){        	nLine = Double.POSITIVE_INFINITY;        }        return nLine;    }    static double getAVGValue(LightNode node, TargetGettingKey key) {        double nline = 0;        for (int i = 0; i < node.getChildLength(); i++) {            nline += node.getChild(i).getSummaryValue(key).doubleValue();        }        nline = nline / node.getChildLength();        return nline;    }    public static void buildParentAndSiblingRelation(LightNode root) {        buildSiblingDeepRelation(root);        buildParentDeepRelation(root);        setSiblingBetweenFirstAndLastChild(root);    }    public static void setSiblingBetweenFirstAndLastChild(LightNode root) {        for (int i = 0; i < root.getChildLength(); i++) {            LightNode parent = root.getChild(i);            if (parent.getChildLength() != 0) {                setSiblingBetweenFirstAndLastChild(parent);            }            if (i + 1 < root.getChildLength()) {                LightNode nextParent = root.getChild(i + 1);                LightNode tmpLastChild = parent.getLastChild();                LightNode tmpFirstChild = nextParent.getFirstChild();                while (tmpLastChild != null && tmpFirstChild != null) {                    tmpLastChild.setSibling(tmpFirstChild);                    tmpLastChild = tmpLastChild.getLastChild();                    tmpFirstChild = tmpFirstChild.getFirstChild();                }            }        }    }    private static void buildSiblingDeepRelation(LightNode root) {        buildSiblingRelation(root);        for (int i = 0; i < root.getChildLength(); i++) {            buildSiblingRelation(root.getChild(i));        }    }    private static void buildSiblingRelation(LightNode root) {        if (root.getChildLength() < 2) {            return;        }        LightNode child = root.getChild(0);        for (int i = 1; i < root.getChildLength(); i++) {            LightNode next = root.getChild(i);            child.setSibling(next);            child = next;        }    }    private static void buildParentDeepRelation(LightNode root) {        buildParentRelation(root);        for (int i = 0; i < root.getChildLength(); i++) {            buildParentRelation(root.getChild(i));        }    }    private static void buildParentRelation(LightNode root) {        for (int i = 0; i < root.getChildLength(); i++) {            LightNode child = root.getChild(i);            child.setParent(root);        }    }    public static void copyLightField(LightNode copiedNode, LightNode originalNode) {        copiedNode.setData(originalNode.getData());        copiedNode.setShowValue(originalNode.getShowValue());        copiedNode.setSummaryValueMap(originalNode.getSummaryValueMap());        copiedNode.setComparator(originalNode.getComparator());    }    public static void copyIndexMap(LightNode copiedNode, LightNode originalNode) {        if (originalNode.getTargetIndexValueMap() != null) {            copiedNode.setTargetIndexValueMap(originalNode.getTargetIndexValueMap());        }        if (originalNode.getGroupValueIndexMap() != null) {            copiedNode.setGroupValueIndexMap(originalNode.getGroupValueIndexMap());        }    }    public static void reCalculateIndex(LightNode node, TargetGettingKey key) {        if (isLeafNode(node)) {            return;        }        GroupValueIndex groupValueIndex = node.getTargetIndexValueMap().get(key);        if (groupValueIndex == null) {            return;        }        GroupValueIndex orGroupValueIndex = GVIFactory.createAllEmptyIndexGVI();        for (int i = 0; i < node.getChildLength(); i++) {            LightNode child = node.getChild(i);            reCalculateIndex(child, key);            GroupValueIndex childGroupValueIndex = child.getTargetIndexValueMap().get(key);            if (childGroupValueIndex == null) {                node.getTargetIndexValueMap().put(key, null);            } else {                orGroupValueIndex = orGroupValueIndex.OR(childGroupValueIndex);            }        }        node.getTargetIndexValueMap().put(key, orGroupValueIndex);        node.getGroupValueIndexMap().put(key,orGroupValueIndex);    }    private static boolean isLeafNode(LightNode node) {        return node.getChildLength() == 0;    }}