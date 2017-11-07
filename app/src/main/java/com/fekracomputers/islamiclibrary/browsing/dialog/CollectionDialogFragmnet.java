package com.fekracomputers.islamiclibrary.browsing.dialog;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.fekracomputers.islamiclibrary.R;
import com.fekracomputers.islamiclibrary.browsing.controller.BookCollectionsController;
import com.fekracomputers.islamiclibrary.model.BookCollectionInfo;
import com.fekracomputers.islamiclibrary.model.BooksCollection;
import com.fekracomputers.islamiclibrary.utility.Util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Mohammad on 1/11/2017.
 */

public class CollectionDialogFragmnet extends DialogFragment {

    public static final java.lang.String TAG_FRAGMENT_COLLECTION = "CollectionDialogFragmnet";
    private static final String KEY_BOOK_ID = "CollectionDialogFragmnet.KEY_BOOK_ID";
    private static final String KEY_COLLECTION_IDS = "collectionDialogFragmnet.KEY_COLLECTION_IDS";
    private BookCollectionInfo bookCollectionInfo;
    private ArrayList<Integer> collectionsIdsArrayList;
    private CollectionDialogFragmnetListener listener;
    private ArrayList<BooksCollection> bookCollections;
    private BookCollectionsController bookCollectionsController;


    public static CollectionDialogFragmnet newInstance(BookCollectionInfo bookCollectionInfo) {
        CollectionDialogFragmnet frag = new CollectionDialogFragmnet();
        Bundle args = new Bundle();
        int bookId = bookCollectionInfo.getBookId();
        args.putInt(KEY_BOOK_ID, bookId);
        args.putIntegerArrayList(KEY_COLLECTION_IDS, new ArrayList<>(bookCollectionInfo.getBooksCollectionIds()));
        frag.setArguments(args);
        return frag;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.ThemeOverlay_AppCompat_Dialog_Alert);
        Bundle arguments = getArguments();
        collectionsIdsArrayList = arguments.getIntegerArrayList(KEY_COLLECTION_IDS);
        Set<Integer> bookIdCollectionSet;
        bookIdCollectionSet = collectionsIdsArrayList == null ? new HashSet<>(0) : new HashSet<>(collectionsIdsArrayList);
        int bookId = arguments.getInt(KEY_BOOK_ID);
        bookCollectionInfo = new BookCollectionInfo(bookIdCollectionSet, bookId);
        bookCollectionsController = new BookCollectionsController(getContext());
        bookCollections = bookCollectionsController.getAllBookCollections(getContext(), true, true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.collection_fragment_dialog, container, false);

        Fragment parentFragment = getParentFragment();
        if (parentFragment instanceof CollectionDialogFragmnetListener) {
            listener = (CollectionDialogFragmnetListener) parentFragment;
        } else {
            throw new RuntimeException(parentFragment.toString()
                    + " must implement CollectionDialogFragmnetListener");
        }

        RecyclerView collectionRecyclerView = rootView.findViewById(R.id.collection_recycler_view);
        CollectionRecyclerViewAdapter adapter = new CollectionRecyclerViewAdapter();
        adapter.setHasStableIds(true);
        collectionRecyclerView.setHasFixedSize(true);
        collectionRecyclerView.setAdapter(adapter);
        collectionRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        Button okButton = rootView.findViewById(R.id.btn_ok);
        okButton.setOnClickListener(v -> {
            listener.collectionChanged(bookCollectionInfo);
            bookCollectionsController.updateCollectionStatus(bookCollectionInfo);
            dismiss();
        });
        EditText newCollectionName = rootView.findViewById(R.id.new_collection_edit_text);
        ImageButton addCollectionButton = rootView.findViewById(R.id.add_collection);
        addCollectionButton.setOnClickListener(v ->
        {
            BooksCollection booksCollection = bookCollectionsController.createNewCollection(newCollectionName.getText().toString());
            // bookCollectionInfo.addToCollection(booksCollection.getCollectionsId());
            bookCollections = bookCollectionsController.getAllBookCollections(getContext(), true, true);
            adapter.notifyDataSetChanged();
            collectionRecyclerView.scrollToPosition(bookCollections.size() - 1);
            newCollectionName.setText("");
        });

        boolean enabled = !newCollectionName.getText().toString().isEmpty();
        setEnabledAddButton(addCollectionButton, enabled);
        //addCollectionButton.setClickable(!newCollectionName.getText().toString().isEmpty());
        newCollectionName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() != 0) {
                    setEnabledAddButton(addCollectionButton, true);
                } else {
                    setEnabledAddButton(addCollectionButton, false);
                }
            }
        });

        Button cancelButton = rootView.findViewById(R.id.btn_cancel);
        cancelButton.setOnClickListener(v -> dismiss());

        return rootView;
    }

    private void setEnabledAddButton(ImageButton addCollectionButton, boolean enabled) {
        Util.setImageButtonEnabled(getContext(),
                enabled,
                addCollectionButton, R.drawable.ic_add_black_24dp);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
    }

    public interface CollectionDialogFragmnetListener {
        void collectionChanged(BookCollectionInfo bookCollectionInfo);
    }

    private class CollectionRecyclerViewAdapter extends RecyclerView.Adapter<CollectionRecyclerViewAdapter.ViewHolder> {
        public CollectionRecyclerViewAdapter() {

        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_collection_with_check_box, parent, false);
            return new CollectionRecyclerViewAdapter.ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            BooksCollection booksCollection = bookCollections.get(position);
            holder.booksCollection = booksCollection;
            holder.collectionNameTextView.setText(booksCollection.getName());
            holder.checkBox.setChecked(bookCollectionInfo.doBelongTo(booksCollection));
        }

        @Override
        public int getItemCount() {
            return bookCollections.size();
        }

        public long getItemId(int position) {
            return bookCollections.get(position).getCollectionsId();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final TextView collectionNameTextView;
            final CheckBox checkBox;
            BooksCollection booksCollection;

            ViewHolder(View view) {
                super(view);
                collectionNameTextView = view.findViewById(R.id.title);
                checkBox = view.findViewById(R.id.checkbox);
                checkBox.setOnClickListener(v ->
                        bookCollectionInfo
                                .setBelongToCollection(
                                        booksCollection.getCollectionsId(),
                                        ((CheckBox) v).isChecked()));
            }
        }

    }
}
