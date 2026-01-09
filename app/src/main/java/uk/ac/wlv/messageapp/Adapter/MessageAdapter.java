package uk.ac.wlv.messageapp.Adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import uk.ac.wlv.messageapp.Model.Message;
import uk.ac.wlv.messageapp.R;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private final Context context;
    private final List<Message> messages;

    public MessageAdapter(Context context, List<Message> messages) {
        this.context = context;
        this.messages = messages;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_row, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messages.get(position);

        holder.title.setText(message.getTitle());
        holder.content.setText(message.getContent());

        String imagePath = message.getImagePath();

        if (imagePath != null && !imagePath.isEmpty()) {
            holder.imageView.setVisibility(View.VISIBLE);

            // Glide can identify String paths, Uris, and Files automatically.
            // We can simplify the logic significantly.

            Object imageSource; // Glide can load Object types (Uri, String, File)

            if (imagePath.startsWith("content://")) {
                imageSource = Uri.parse(imagePath);
            } else if (imagePath.startsWith("http")) {
                imageSource = imagePath;
            } else {
                // Assume local file path
                imageSource = new File(imagePath);
            }

            Glide.with(context)
                    .load(imageSource)
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background) // If provider is missing, this shows instead of crashing
                    .into(holder.imageView);

        } else {
            // No image path
            holder.imageView.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView title, content;
        ImageView imageView;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.itemTitle);
            content = itemView.findViewById(R.id.itemContent);
            imageView = itemView.findViewById(R.id.itemImage);
        }
    }
}
