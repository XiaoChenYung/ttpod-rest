package test.netty.query;

import com.ttpod.netty.Client;
import com.ttpod.netty.rpc.RequestBean;
import com.ttpod.netty.rpc.client.ClientHandler;
import com.ttpod.netty.rpc.client.DefaultClientHandler;
import com.ttpod.netty.rpc.client.OutstandingContainer;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

/**
 * date: 14-1-28 下午2:16
 *
 * @author: yangyang.cong@ttpod.com
 */
public class Notify_Future {
    public static void main(String[] args) throws Exception {
        Client client = new Client(new InetSocketAddress("127.0.0.1", 6666),
                new ClientInitializer());
        System.out.println("Begin Loop");
        // Read commands from the stdin.
        final ClientHandler handler = client.getChannel().pipeline().get(DefaultClientHandler.class);
        final int THREADS = OutstandingContainer.UNSIGN_SHORT_OVER_FLOW;
        ExecutorService exe = Executors.newFixedThreadPool(Math.min(1024, THREADS));

        int NotifyTotal = 0;
        int FutureTotal = 0;
        int TIMES = 15;
        int HOT_TOTAL = TIMES - 5;
        Benchmark notify = new Benchmark("Notify",handler,exe,THREADS);
        Benchmark future = new Benchmark("Future",handler,exe,THREADS){
            protected void rpcCall(RequestBean req) {
                try {
                    handler.rpc(req, 1000);
                } catch (TimeoutException e) {
                    e.printStackTrace();
                }
            }
        };
        for (int j = TIMES; j > 0; j--) {
            int cost = notify.costMills();
            if (j <= HOT_TOTAL) {
                NotifyTotal += cost;
            }
            cost = future.costMills();
            if (j <= HOT_TOTAL) {
                FutureTotal += cost;
            }
        }

        System.out.println("===========================END========================");
        int cost = NotifyTotal / HOT_TOTAL;
        System.out.println("Notify  cov ,cost : " + cost + " ms, rps : " + THREADS * 1000  / cost);
        cost = FutureTotal / HOT_TOTAL;
        System.out.println("Future  cov ,cost : " + cost + " ms, rps : " + THREADS * 1000  / cost);
        client.close();
        exe.shutdown();

    }
}
