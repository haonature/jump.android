package com.janrain.android.engage.ui;
/*
 *  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *  Copyright (c) 2011, Janrain, Inc.
 *
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without modification,
 *  are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation and/or
 *    other materials provided with the distribution.
 *  * Neither the name of the Janrain, Inc. nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 */

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.janrain.android.R;
import com.janrain.android.engage.JRNativeAuth;
import com.janrain.android.engage.session.JRProvider;
import com.janrain.android.engage.types.JRDictionary;
import com.janrain.android.utils.LogUtils;

public class JRNativeAuthFragment extends JRUiFragment {
    private JRProvider mProvider;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.jr_provider_native, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (isSignOutFlow()) {
            signOut();
        } else if (isRevokeFlow()) {
            revoke();
        } else {
            signIn();
        }
    }

    private void signOut() {
        final JRProvider provider = mSession.getProviderByName(getSignOutProviderName());
        JRNativeAuth.NativeProvider nativeProvider = JRNativeAuth.createNativeProvider(provider,
                getActivity(), getSignOutOrRevokeCallback());
        nativeProvider.signOut();
    }

    private void revoke() {
        if (getRevokeProviderName().equals("googleplus")) {
            final JRProvider provider = mSession.getProviderByName(getRevokeProviderName());
            JRNativeAuth.NativeProvider nativeProvider = JRNativeAuth.createNativeProvider(provider,
                    getActivity(), getSignOutOrRevokeCallback());
            nativeProvider.revoke();
        }
    }

    private void signIn() {
        mProvider = mSession.getCurrentlyAuthenticatingProvider();
        JRNativeAuth.NativeProvider nativeProvider = JRNativeAuth.createNativeProvider(mProvider, getActivity(),
                new JRNativeAuth.NativeAuthCallback() {
                    @Override
                    public void onSuccess(JRDictionary payload) {
                        mSession.saveLastUsedAuthProvider();
                        mSession.triggerAuthenticationDidCompleteWithPayload(payload);
                        //final JRSession session = JRSession.getInstance();
                        mSession.addNativeProvider(mProvider.getName());
                        finishFragmentWithResult(Activity.RESULT_OK);
                    }

                    @Override
                    public void onFailure(final String message, JRNativeAuth.NativeAuthError errorCode,
                                          Exception exception, boolean shouldTryWebViewAuthentication) {
                        super.onFailure(message, errorCode, exception, shouldTryWebViewAuthentication);
                        if (errorCode.equals(JRNativeAuth.NativeAuthError.FACEBOOK_SESSION_IS_CLOSED)) {
                            finishFragmentWithResult(Activity.RESULT_OK);
                        } else {
                            finishFragment();
                        }
                    }

                    @Override
                    public boolean shouldTriggerAuthenticationDidCancel() {
                        return isSpecificProviderFlow();
                    }

                    @Override
                    public void tryWebViewAuthentication() {
                        mProvider = mSession.getCurrentlyAuthenticatingProvider();
                        startWebViewAuthForProvider(mProvider);
                    }
                });
        mSession.setCurrentNativeProvider(nativeProvider);
        nativeProvider.startAuthentication();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        LogUtils.logd(TAG, "requestCode: " + requestCode + " resultCode: " + resultCode);
        if (requestCode == JRUiFragment.REQUEST_WEBVIEW) {
            finishFragmentWithResult(resultCode);
        }
        if (mSession.getCurrentNativeProvider() != null) {
            mSession.getCurrentNativeProvider().onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onBackPressed() {
        finishFragmentWithResult(Activity.RESULT_CANCELED);
    }

    private boolean isSignOutFlow() {
        return getSignOutProviderName() != null;
    }

    private String getSignOutProviderName() {
        return getArguments().getString(JRFragmentHostActivity.JR_SIGN_OUT_PROVIDER);
    }

    private boolean isRevokeFlow() {
        return getRevokeProviderName() != null;
    }

    private String getRevokeProviderName() {
        return getArguments().getString(JRFragmentHostActivity.JR_REVOKE_PROVIDER);
    }

    private JRNativeAuth.NativeAuthCallback getSignOutOrRevokeCallback() {
        return new JRNativeAuth.NativeAuthCallback() {
            @Override
            public void onSuccess(JRDictionary payload) {
                finishFragmentWithResult(Activity.RESULT_OK);
            }

            @Override
            public void onFailure(final String message, JRNativeAuth.NativeAuthError errorCode,
                                  Exception exception, boolean shouldTryWebViewAuthentication) {
                finishFragment();
            }

            @Override
            public boolean shouldTriggerAuthenticationDidCancel() {
                return false;
            }

            @Override
            public void tryWebViewAuthentication() {
                finishFragment();
            }
        };
    }
}
