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
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;

import uk.ac.wlv.messageapp.Model.Message;
import uk.ac.wlv.messageapp.R;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private final List<Message> messageList;
    private final Context context;

    public MessageAdapter(Context context, List<Message> messageList) {
        this.context = context;
        this.messageList = messageList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Message message = messageList.get(position);
        holder.txtTitle.setText(message.getTitle());
        holder.txtContent.setText(message.getContent());

        String imagePath = message.getImagePath();
        if (imagePath != null && !imagePath.isEmpty()) {
            holder.imageView.setVisibility(View.VISIBLE);

            try {
                if (imagePath.startsWith("content://")) {
                    // Handle content URIs safely using temp file
                    File file = getFileFromContentUri(context, Uri.parse(imagePath));
                    if (file != null) {
                        Glide.with(context)
                                .load(file)
                                .placeholder(R.drawable.ic_launcher_background)
                                .error(R.drawable.ic_launcher_background)
                                .into(holder.imageView);
                    } else {
                        holder.imageView.setVisibility(View.GONE);
                    }

                } else if (imagePath.startsWith("/")) {
                    // Local file path
                    File file = new File(imagePath);
                    if (file.exists()) {
                        Glide.with(context)
                                .load(file)
                                .placeholder(R.drawable.ic_launcher_background)
                                .error(R.drawable.ic_launcher_background)
                                .into(holder.imageView);
                    } else {
                        holder.imageView.setVisibility(View.GONE);
                    }

                } else if (imagePath.startsWith("http://") || imagePath.startsWith("https://")) {
                    // Network URL
                    Glide.with(context)
                            .load(imagePath)
                            .placeholder(R.drawable.ic_launcher_background)
                            .error(R.drawable.ic_launcher_background)
                            .into(holder.imageView);
                } else {
                    holder.imageView.setVisibility(View.GONE);
                }
            } catch (Exception e) {
                e.printStackTrace();
                holder.imageView.setVisibility(View.GONE);
            }
        } else {
            holder.imageView.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitle, txtContent;
        ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitle = itemView.findViewById(R.id.itemTitle);
            txtContent = itemView.findViewById(R.id.itemContent);
            imageView = itemView.findViewById(R.id.itemImage);
        }
    }

    /**
     * Copy content URI to a temporary file in app cache.
     * Returns the File or null if failed.
     */
    public static File getFileFromContentUri(Context context, Uri uri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;

            File tempFile = File.createTempFile("temp_image", ".jpg", context.getCacheDir());
            tempFile.deleteOnExit();

            FileOutputStream out = new FileOutputStream(tempFile);
            byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            out.close();
            inputStream.close();

            return tempFile;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
