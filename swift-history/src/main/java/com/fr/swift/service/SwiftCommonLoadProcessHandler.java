package com.fr.swift.service;

import com.fr.swift.basics.AsyncRpcCallback;
import com.fr.swift.basics.Invoker;
import com.fr.swift.basics.InvokerCreater;
import com.fr.swift.basics.RpcFuture;
import com.fr.swift.basics.URL;
import com.fr.swift.basics.annotation.Target;
import com.fr.swift.basics.base.handler.AbstractProcessHandler;
import com.fr.swift.basics.base.selector.UrlSelector;
import com.fr.swift.basics.handler.CommonLoadProcessHandler;
import com.fr.swift.cluster.entity.ClusterEntity;
import com.fr.swift.cluster.service.ClusterSwiftServerService;
import com.fr.swift.config.service.SwiftClusterSegmentService;
import com.fr.swift.context.SwiftContext;
import com.fr.swift.event.base.EventResult;
import com.fr.swift.segment.SegmentKey;
import com.fr.swift.util.MonitorUtil;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

/**
 * This class created on 2018/11/6
 *
 * @author Lucifer
 * @description
 * @since Advanced FineBI 5.0
 */
public class SwiftCommonLoadProcessHandler extends AbstractProcessHandler<Map<URL, Map<String, List<String>>>> implements CommonLoadProcessHandler<Map<URL, Map<String, List<String>>>> {

    public SwiftCommonLoadProcessHandler(InvokerCreater invokerCreater) {
        super(invokerCreater);
    }

    /**
     * @param method
     * @param target
     * @param args
     * @return
     * @throws Throwable
     */
    @Override
    public Object processResult(Method method, Target target, Object... args) throws Throwable {
        Class proxyClass = method.getDeclaringClass();
        Class<?>[] parameterTypes = method.getParameterTypes();
        String methodName = method.getName();
        try {
            MonitorUtil.start();
            String sourceKey = (String) args[0];
            Map<URL, Map<String, List<String>>> urlMap = processUrl(target, args);

            final List<EventResult> resultList = new ArrayList<EventResult>();
            final CountDownLatch latch = new CountDownLatch(urlMap.size());
            for (final Map.Entry<URL, Map<String, List<String>>> urlMapEntry : urlMap.entrySet()) {

                Invoker invoker = invokerCreater.createAsyncInvoker(proxyClass, urlMapEntry.getKey());
                RpcFuture rpcFuture = (RpcFuture) invoke(invoker, proxyClass, method, methodName, parameterTypes, sourceKey, urlMapEntry.getValue());
                rpcFuture.addCallback(new AsyncRpcCallback() {
                    @Override
                    public void success(Object result) {
                        EventResult eventResult = new EventResult(urlMapEntry.getKey().getDestination().getId(), true);
                        resultList.add(eventResult);
                        latch.countDown();
                    }

                    @Override
                    public void fail(Exception e) {
                        EventResult eventResult = new EventResult(urlMapEntry.getKey().getDestination().getId(), false);
                        eventResult.setError(e.getMessage());
                        resultList.add(eventResult);
                        latch.countDown();
                    }
                });
            }
            return resultList;
        } finally {
            MonitorUtil.finish(methodName);
        }
    }

    /**
     * args：需要load的seg信息
     * args[0]:sourcekey
     * args[1]:<segkey,uri string>
     * 根据传入的seg信息，遍历所有history节点，找到每个history节点的seg
     * 检验该seg是否在需要args中，是则needload，否则不需要。
     *
     * @param target
     * @param args
     * @return 远程url
     */
    @Override
    public Map<URL, Map<String, List<String>>> processUrl(Target target, Object... args) {
        SwiftClusterSegmentService clusterSegmentService = SwiftContext.get().getBean(SwiftClusterSegmentService.class);
        Map<String, ClusterEntity> services = ClusterSwiftServerService.getInstance().getClusterEntityByService(ServiceType.HISTORY);

        String sourceKey = (String) args[0];
        Map<String, List<String>> uris = (Map<String, List<String>>) args[1];

        if (null == services || services.isEmpty()) {
            throw new RuntimeException("Cannot find history service");
        }
        Map<URL, Map<String, List<String>>> resultMap = new HashMap<URL, Map<String, List<String>>>();
        for (Map.Entry<String, ClusterEntity> servicesEntry : services.entrySet()) {
            String clusterId = servicesEntry.getKey();
            Map<String, List<SegmentKey>> map = clusterSegmentService.getOwnSegments(clusterId);
            List<SegmentKey> list = map.get(sourceKey);
            Set<String> needLoad = new HashSet<String>();
            if (!list.isEmpty()) {
                for (SegmentKey segmentKey : list) {
                    String segKey = segmentKey.toString();
                    if (uris.containsKey(segKey)) {
                        needLoad.addAll(uris.get(segKey));
                    }
                }
            }
            if (!needLoad.isEmpty()) {
                Map<String, List<String>> loadMap = new HashMap<String, List<String>>();
                loadMap.put(sourceKey, new ArrayList<String>(needLoad));
                resultMap.put(UrlSelector.getInstance().getFactory().getURL(clusterId), loadMap);
            }
        }
        return resultMap;
    }
}
