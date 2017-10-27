package za.co.woolworths.financial.services.android.models.dto;

import android.net.Uri;

/**
 * Created by W7099877 on 2017/10/19.
 */

public class Document {

	public String name;
	public Uri uri;
	public long size;
	public int progress;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Uri getUri() {
		return uri;
	}

	public void setUri(Uri uri) {
		this.uri = uri;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public int getProgress() {
		return progress;
	}

	public void setProgress(int progress) {
		this.progress = progress;
	}
}
