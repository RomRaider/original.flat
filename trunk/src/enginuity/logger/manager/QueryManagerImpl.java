package enginuity.logger.manager;

import enginuity.logger.definition.EcuParameter;
import enginuity.logger.query.LoggerCallback;
import enginuity.logger.query.RegisteredQuery;
import enginuity.logger.query.RegisteredQueryImpl;
import static enginuity.util.ParamChecker.checkNotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"FieldCanBeLocal"})
public final class QueryManagerImpl implements QueryManager {
    private final Map<EcuParameter, RegisteredQuery> queryMap = Collections.synchronizedMap(new HashMap<EcuParameter, RegisteredQuery>());
    private final List<RegisteredQuery> addList = new ArrayList<RegisteredQuery>();
    private final List<EcuParameter> removeList = new ArrayList<EcuParameter>();
    private final TransmissionManager txManager;
    private boolean stop = false;

    public QueryManagerImpl(TransmissionManager txManager) {
        checkNotNull(txManager, "txManager");
        this.txManager = txManager;
    }

    public synchronized void addQuery(EcuParameter ecuParam, LoggerCallback callback) {
        checkNotNull(ecuParam, callback);
        addList.add(new RegisteredQueryImpl(ecuParam, callback));
    }

    public synchronized void removeQuery(EcuParameter ecuParam) {
        removeList.add(ecuParam);
    }

    public void run() {
        System.out.println("QueryManager started.");

        long start = System.currentTimeMillis();
        int count = 0;

        try {
            txManager.start();
            stop = false;
            while (!stop) {
                updateQueryList();
                txManager.sendQueries(queryMap.values());
                count++;
            }
        } finally {
            txManager.stop();
        }
        System.out.println("QueryManager stopped.");

        //TODO: this is not real nice - add some decent performance measurements later
        System.out.println("Total queries sent  = " + count);
        double duration = ((double) (System.currentTimeMillis() - start)) / 1000D;
        System.out.println("Queries per second  = " + (((double) count) / duration));
        System.out.println("Avg. Query Time (s) = " + (duration / ((double) count)));
    }

    public void stop() {
        stop = true;
    }

    private synchronized void updateQueryList() {
        addQueries();
        removeQueries();
    }

    private void addQueries() {
        for (RegisteredQuery registeredQuery : addList) {
            queryMap.put(registeredQuery.getEcuParam(), registeredQuery);
        }
        addList.clear();
    }

    private void removeQueries() {
        for (EcuParameter ecuParam : removeList) {
            queryMap.remove(ecuParam);
        }
        removeList.clear();
    }

}
