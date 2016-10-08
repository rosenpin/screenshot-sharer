// Generated code from Butter Knife. Do not modify!
package com.tomer.screenshotsharer;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.view.View;
import android.widget.ToggleButton;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import java.lang.IllegalStateException;
import java.lang.Override;

public class MainActivity_ViewBinding<T extends MainActivity> implements Unbinder {
  protected T target;

  @UiThread
  public MainActivity_ViewBinding(T target, View source) {
    this.target = target;

    target.assistant = Utils.findRequiredViewAsType(source, R.id.assistant, "field 'assistant'", ToggleButton.class);
    target.storage = Utils.findRequiredViewAsType(source, R.id.storage, "field 'storage'", ToggleButton.class);
    target.preview = Utils.findRequiredViewAsType(source, R.id.preview, "field 'preview'", ToggleButton.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    T target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");

    target.assistant = null;
    target.storage = null;
    target.preview = null;

    this.target = null;
  }
}
