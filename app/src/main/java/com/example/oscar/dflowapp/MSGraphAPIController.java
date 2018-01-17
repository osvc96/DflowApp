package com.example.oscar.dflowapp;

/**
 * Created by juanjose on 1/17/18.
 */

import android.content.Context;

import com.example.oscar.dflowapp.vo.BodyVO;
import com.example.oscar.dflowapp.vo.EmailAddressVO;
import com.example.oscar.dflowapp.vo.MessageVO;
import com.example.oscar.dflowapp.vo.MessageWrapper;
import com.example.oscar.dflowapp.vo.ToRecipientsVO;

import okhttp3.Interceptor;
import retrofit2.Call;


/**
 * Handles the creation of the message and contacting the
 * mail service to send the message. The app must have
 * connected to Office 365 and discovered the mail service
 * endpoints before using the createDraftMail method.
 */
public class MSGraphAPIController {

    private MSGraphAPIService mMSGraphAPIService;

    public MSGraphAPIController(Context context) {
        mMSGraphAPIService = new RESTHelper()
                .getRetrofit(context)
                .create(MSGraphAPIService.class);
    }

    public MSGraphAPIController(Interceptor interceptor) {
        mMSGraphAPIService = new RESTHelper()
                .getRetrofit(interceptor)
                .create(MSGraphAPIService.class);
    }

    /**
     * Sends an email message using the Microsoft Graph API on Office 365. The mail is sent
     * from the address of the signed in user.
     *
     * @param emailAddress The recipient email address.
     * @param subject      The subject to use in the mail message.
     * @param body         The body of the message.
     */
    public Call<Void> sendMail(
            final String emailAddress,
            final String subject,
            final String body) {
        // create the email
        MessageWrapper msg = createMailPayload(subject, body, emailAddress);

        // send it using our service
        return mMSGraphAPIService.sendMail("application/json", msg);
    }

    private MessageWrapper createMailPayload(
            String subject,
            String body,
            String address) {
        EmailAddressVO emailAddressVO = new EmailAddressVO();
        emailAddressVO.mAddress = address;

        ToRecipientsVO toRecipientsVO = new ToRecipientsVO();
        toRecipientsVO.emailAddress = emailAddressVO;

        BodyVO bodyVO = new BodyVO();
        bodyVO.mContentType = "HTML";
        bodyVO.mContent = body;

        MessageVO sampleMsg = new MessageVO();
        sampleMsg.mSubject = subject;
        sampleMsg.mBody = bodyVO;
        sampleMsg.mToRecipients = new ToRecipientsVO[]{toRecipientsVO};

        return new MessageWrapper(sampleMsg);
    }

}