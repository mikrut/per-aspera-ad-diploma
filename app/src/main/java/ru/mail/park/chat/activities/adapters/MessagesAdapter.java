package ru.mail.park.chat.activities.adapters;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import ru.mail.park.chat.R;
import ru.mail.park.chat.activities.ProfileViewActivity;
import ru.mail.park.chat.activities.views.TitledPicturedViewHolder;
import ru.mail.park.chat.api.ApiSection;
import ru.mail.park.chat.loaders.images.ImageDownloadManager;
import ru.mail.park.chat.models.AttachedFile;
import ru.mail.park.chat.models.Contact;
import ru.mail.park.chat.models.Message;

/**
 * Created by Михаил on 27.03.2016.
 */
public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.ViewHolder> {
    private static final int INCOMING_MESSAGE = 0;
    private static final int OUTGOING_MESSAGE = 1;

    private final List<Message> messagesSet;
    private final String ownerUID;
    private ImageDownloadManager imageDownloadManager;

    public MessagesAdapter(@NonNull List<Message> messagesSet, @NonNull String ownerUID) {
        this.messagesSet = messagesSet;
        this.ownerUID = ownerUID;
    }

    public void setImageDownloadManager(ImageDownloadManager imageDownloadManager) {
        ImageDownloadManager old = this.imageDownloadManager;
        this.imageDownloadManager = imageDownloadManager;
        if (old == null && imageDownloadManager != null) {
            notifyDataSetChanged();
        }
    }

    public class ViewHolder extends TitledPicturedViewHolder {
        private final TextView messageText;
        private final ListView attachments;
        public final ImageView clockImageView;
        private final TextView messageTime;

        private String oldUrl;

        private String authorUID;

        public ViewHolder(View itemView) {
            super(itemView);
            clockImageView = (ImageView) itemView.findViewById(R.id.clockImageView);
            messageText = (TextView) itemView.findViewById(R.id.messageText);
            attachments = (ListView) itemView.findViewById(R.id.attachments_list_view);
            messageTime = (TextView) itemView.findViewById(R.id.messageTime);
        }

        public void setMessage(@NonNull final Message message, final int messagePosition) {
            final String imageURL = message.getImageURL();
            if (oldUrl == null || !oldUrl.equals(imageURL)) {
                if (imageURL != null && !imageURL.equals("false") && imageDownloadManager != null) {
                    try {
                        final URL url = new URL(ApiSection.SERVER_URL + imageURL);
                        image.post(new Runnable() {
                            @Override
                            public void run() {
                                imageDownloadManager.setImage(ViewHolder.this, url);
                                oldUrl = imageURL;
                            }
                        });
                    } catch (MalformedURLException e) {
                        Log.w(ContactAdapter.class.getSimpleName(), e.getLocalizedMessage());
                    }
                } else {
                    setImage(null);
                }
            }

            setTitle(message.getTitle());
            authorUID = message.getUid();
            if (message.getMessageBody() != null && !message.getMessageBody().equals("")) {
                messageText.setText(message.getMessageBody());
                messageText.setVisibility(View.VISIBLE);
            } else {
                messageText.setVisibility(View.GONE);
            }
            attachments.setAdapter(new FilesSimpleAdapter(itemView.getContext(), message.getFiles()));
            attachments.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    AttachedFile attachment = message.getFiles().get(position);
                    boolean fileIsDownloadedAndExists = (attachment.getFromFileSystem(view.getContext()) != null);
                    Log.v("File download", fileIsDownloadedAndExists ? "not needed" : "needed");
                    if (fileIsDownloadedAndExists) {
                        attachment.openInNewActivity(view.getContext());
                    } else {
                        attachment.download(view.getContext(), MessagesAdapter.this, messagePosition);
                    }
                }
            });

            image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), ProfileViewActivity.class);
                    intent.putExtra(ProfileViewActivity.UID_EXTRA, authorUID);
                    v.getContext().startActivity(intent);
                }
            });

            clockImageView.setVisibility(message.isAcknowledged() ? View.GONE : View.VISIBLE);

            if (message.getFiles().size() > 0) {
                attachments.setVisibility(View.VISIBLE);
            } else {
                attachments.setVisibility(View.GONE);
            }

            // TODO: message time
            if (false) {
                messageTime.setVisibility(View.VISIBLE);
                messageTime.setText("Message time text");
            } else {
                messageTime.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        @android.support.annotation.LayoutRes int resource;
        switch (viewType) {
            case INCOMING_MESSAGE:
                resource = R.layout.element_message_left;
                break;
            case OUTGOING_MESSAGE:
            default:
                resource = R.layout.element_message_right;
                break;
        }
        View messageView = LayoutInflater.from(parent.getContext())
                .inflate(resource, parent, false);
        return new ViewHolder(messageView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Message message = messagesSet.get(position);
        holder.setMessage(message, position);
    }

    @Override
    public int getItemCount() {
        return messagesSet.size();
    }

    @Override
    public int getItemViewType(int position) {
        String senderUID = messagesSet.get(position).getUid();
        if (senderUID.equals(ownerUID)) {
            return OUTGOING_MESSAGE;
        } else {
            return INCOMING_MESSAGE;
        }
    }


}
