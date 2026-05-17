/**
 *
 *  @author Mysiewicz Michał s32528
 *
 */

package zad1;


import java.util.List;
import java.util.concurrent.FutureTask;

public
    class ChatClientTask extends FutureTask<ChatClient> {

    private ChatClient c;

    public ChatClientTask(ChatClient c, List<String> msgs, int wait) {
        super(() -> {
            c.login();
            if(wait > 0){
                Thread.sleep(wait);
            }

            for(String msg : msgs){
                c.send(msg);
                if(wait > 0){
                    Thread.sleep(wait);
                }
            }

            c.logout();
            if(wait > 0){
                Thread.sleep(wait);
            }
            return c;
        });
        this.c = c;
    }

    public static ChatClientTask create(ChatClient c, List<String> msgs, int wait){
        return new ChatClientTask(c, msgs, wait);
    }
    public ChatClient getClient(){
        return c;
    }
}  
