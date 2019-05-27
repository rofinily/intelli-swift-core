package com.fr.swift.repository.manager;

import com.fr.swift.SwiftContext;
import com.fr.swift.log.SwiftLoggers;
import com.fr.swift.repository.PackageConnectorConfig;
import com.fr.swift.repository.SwiftRepository;
import com.fr.swift.repository.exception.RepoNotFoundException;
import com.fr.swift.repository.impl.SwiftRepositoryImpl;
import com.fr.swift.service.SwiftRepositoryConfService;

/**
 * @author yee
 * @date 2018/5/28
 */
public class SwiftRepositoryManager {
    private static SwiftRepository currentRepository = null;
    private SwiftRepositoryConfService service;

    private SwiftRepositoryManager() {
        service = SwiftContext.get().getBean(SwiftRepositoryConfService.class);
        service.registerListener(new SwiftRepositoryConfService.ConfChangeListener() {
            @Override
            public void change(PackageConnectorConfig change) {
                if (null != currentRepository) {
                    try {
                        currentRepository = new SwiftRepositoryImpl(change);
                    } catch (RepoNotFoundException e) {
                        throw e;
                    } catch (Exception e) {
                        SwiftLoggers.getLogger().warn("Create repository failed. Use default", e);
                        currentRepository = new SwiftRepositoryImpl(change);
                    }
                }
            }
        });
    }

    public static SwiftRepositoryManager getManager() {
        return SingletonHolder.manager;
    }

    public SwiftRepository currentRepo() {
        if (null == currentRepository) {
            synchronized (SwiftRepositoryManager.class) {
                PackageConnectorConfig config = null;
                try {
                    config = SwiftContext.get().getBean(SwiftRepositoryConfService.class).getCurrentRepository();
                } catch (Exception e) {
                    SwiftLoggers.getLogger().warn("Cannot find repository config. Use default.");
                }
                try {
                    currentRepository = new SwiftRepositoryImpl(config);
                } catch (RepoNotFoundException e) {
                    throw e;
                } catch (Exception e) {
                    SwiftLoggers.getLogger().warn("Create repository failed. Use default", e);
                    currentRepository = new SwiftRepositoryImpl(config);
                }
            }
        }
        return currentRepository;
    }

    private static class SingletonHolder {
        private static SwiftRepositoryManager manager = new SwiftRepositoryManager();
    }
}
