package pt.ulisboa.tecnico.cmov.cmovproject;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import pt.ulisboa.tecnico.cmov.cmovproject.model.Message;

//Examples: https://stackoverflow.com/questions/40584424/simple-android-recyclerview-example
//Examples: https://developer.android.com/guide/topics/ui/layout/recyclerview?hl=es-419

class RecycleViewAdapater extends  RecyclerView.Adapter<RecycleViewAdapater.ViewHolder> {

    private List<Message> ListaMensajes;

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView username;
        public TextView message;

        public ViewHolder(View view) {
            super(view);
            username = (TextView) view.findViewById(R.id.nickname);
            message = (TextView) view.findViewById(R.id.message);

        }
    }

    public RecycleViewAdapater(List<Message> ListaMensajes) {
        this.ListaMensajes = ListaMensajes;
    }

    @Override
    public int getItemCount() {
        return ListaMensajes.size();
    }

    @Override
    public RecycleViewAdapater.ViewHolder onCreateViewHolder( ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.message, parent, false);
        return new RecycleViewAdapater.ViewHolder(itemView);
    }
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        //Need to add date
        Message m = ListaMensajes.get(position);
        holder.username.setText(m.getUsername());
        holder.message.setText(m.getMessage());
    }
}

