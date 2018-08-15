package com.dell.bitbucket.merge.checks.hook;


/* DELL.com all rights reserved 2018 */
/* Author Peter M. Gits peter.gits@dell.com */

import com.atlassian.bitbucket.hook.repository.*;
import com.atlassian.bitbucket.pull.PullRequestParticipant;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.setting.*;
import com.atlassian.bitbucket.pull.PullRequest;
import com.atlassian.bitbucket.pull.PullRequestRef;
import com.atlassian.bitbucket.pull.PullRequestParticipant;
import com.atlassian.bitbucket.hook.*;
import com.atlassian.bitbucket.setting.*;
//import com.atlassian.bitbucket.hook.repository.PreReceiveRepositoryHook;
//import com.atlassian.bitbucket.hook.repository.RepositoryHookContext;


import com.atlassian.bitbucket.scm.pull.MergeRequest;
import com.atlassian.bitbucket.scm.pull.MergeRequestCheck;

import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import java.util.StringJoiner;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.*;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.*;


import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.*;
import java.util.Arrays;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

//import com.atlassian.bitbucket.i18n.I18nService;

public class mergeTriggerCheck implements MergeRequestCheck {


    //private final I18nService i18nService;
    //private final PullRequestService pullRequestService;
    final static Logger log = LoggerFactory.getLogger(mergeTriggerCheck.class);

    public mergeTriggerCheck(/*I18nService i18nService, PullRequestService pullRequestService*/) {
        //this.i18nService = i18nService;
        //this.pullRequestService = pullRequestService;
        log.info("initialized mergeTriggerCheck");
        //this is here because it wasn't originally triggering...
    }

    @Override
    public void check(@Nonnull MergeRequest request) {
        log.info(String.format("MergeRequest check called:  repo name = [%s]", request.getPullRequest().getToRef().getRepository().getName()));

        //log.info("running com.dell.bitbucket.merge.checks.hook.mergeTriggerCheck:check");
        MergeRequest mr        = request;
        PullRequest pr         = mr.getPullRequest();
        PullRequestRef prrFrom = pr.getFromRef();
        PullRequestRef prrTo   = pr.getToRef();


        Repository repository = prrTo.getRepository();
        String url = "http://dockerlogin-eqx-02.force10networks.com:8080/mergeTrigger.php";
        String branch = prrTo.getDisplayId();
        String branchFrom = prrFrom.getDisplayId();
        String packageName = repository.getName();

        String keyPackageName  = repository.getProject().getKey();
        String fullPackageName = repository.getProject().getName();
        //String packageRevision = "4.0.0.84";
            if(!keyPackageName.toLowerCase().startsWith("ar")) {//this is not our repo, send it on its way
            log.info("this packageName.key doesn't start with ar");
            log.info(String.format("keyPackageName is equal to [%s][%s], RepositoryHookResult is now accepted", keyPackageName.toLowerCase(), request.getPullRequest().getToRef().getRepository().getName()));
            return ;//RepositoryHookResult.accepted();
        }

        long lpullRequestId = pr.getId();
        String pullRequestId = String.valueOf(lpullRequestId);
        String jsonContainer = String.format("description=%s", pr.getDescription());
        String mergeUser = "Administrator";
        StringBuilder returnResults = new StringBuilder();

        String charset = "UTF-8";  // Or in Java 7 and later, use the constant: java.nio.charset.StandardCharsets.UTF_8.name()
        try {

            URLConnection connection = new URL(url).openConnection();
            HttpURLConnection http = (HttpURLConnection)connection;
            http.setRequestMethod("POST");
            http.setDoOutput(true);

            Map<String,String> arguments = new HashMap();
            arguments.put("from_branch", branchFrom);
            arguments.put("my_branch", branch);
            arguments.put("packageName", packageName);
            arguments.put("packageRevision", "-1");
            arguments.put("jsonContainer", jsonContainer);
            arguments.put("mergeUser", mergeUser);
            arguments.put("pullRequestId", pullRequestId);
            arguments.put("from_branch_full", prrFrom.toString());
            arguments.put("to_branch_full", prrTo.toString());

            StringJoiner sj = new StringJoiner("&");
            for(Map.Entry<String,String> entry : arguments.entrySet())
                sj.add(URLEncoder.encode(entry.getKey(), "UTF-8") + "="
                        + URLEncoder.encode(entry.getValue(), "UTF-8"));
            byte[] out = sj.toString().getBytes(StandardCharsets.UTF_8);
            int length = out.length;

            http.setFixedLengthStreamingMode(length);
            http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            http.setRequestProperty("Accept-Charset", charset);
            http.connect();
            try {
                OutputStream os = http.getOutputStream();
                os.write(out);
            }catch(Exception ee){
                ee.printStackTrace();
            }

            java.io.InputStream response = http.getInputStream();
            java.io.BufferedReader reader = new java.io.BufferedReader(new InputStreamReader(response));
            // And print each line
            String s = null;

            Boolean returnValue = false;
            while ((s = reader.readLine()) != null) {
                log.info(s);
                returnResults.append(s);
            }
            http.disconnect();
            if(returnResults.toString().contains("MergeCheckPassed")) {
                if (returnResults.toString().contains("succeeded")) {
                    log.info("returning success for merge, found MergeCheckPassed + succeeded");
                    return;//RepositoryHookResult.accepted();
                }
            }
        }catch(MalformedURLException me){
            me.printStackTrace();
            //handle the exception if the target service is down, for now allow it to pass through
            //eventually don't
            log.error("malformedURLException: returning success for merge");
            return ;//RepositoryHookResult.accepted();
        }catch(Exception io){
            io.printStackTrace();
            //handle the exception if the target service is down, for now allow it to pass through
            //eventually don't
            log.error("ioException: returning success for merge");
            return ;//RepositoryHookResult.accepted();
        }

        String summaryMsg  = "com.dell.bitbucket.merge.check.detailed:";
        String detailedMsg = returnResults.toString();
               // "The pull request must first pass the Continuum: Merge Trigger Build & the Merge Trigger Smoke Test prior to merging ";
        log.info(String.format("VETO fired: %s:%s", summaryMsg, detailedMsg));
        log.error(String.format("VETO fired:  (Not an error) just need it printed to the log: %s:%s", summaryMsg, detailedMsg));
        mr.veto (summaryMsg, detailedMsg);
    }

}
