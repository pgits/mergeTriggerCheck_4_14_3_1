package com.dell.bitbucket.merge.checks.hook;

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
//import com.atlassian.bitbucket.i18n.I18nService;
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


public class mergeTriggerCheck implements MergeRequestCheck {


    //private final I18nService i18nService;
    //private final PullRequestService pullRequestService;
    final static Logger log = LoggerFactory.getLogger(mergeTriggerCheck.class);

    public mergeTriggerCheck(/*I18nService i18nService, PullRequestService pullRequestService*/) {
        //this.i18nService = i18nService;
        //this.pullRequestService = pullRequestService;
        log.error("initialized mergeTriggerCheck");
    }

    @Override
    public void check(@Nonnull MergeRequest request) {
        log.error(String.format("MergeRequest check called:  repo name = %s", request.getPullRequest().getToRef().getRepository().getName()));


        log.error("running com.dell.bitbucket.merge.checks.hook.mergeTriggerCheck:check");
        MergeRequest mr        = request;
        PullRequest pr         = mr.getPullRequest();
        PullRequestRef prrFrom = pr.getFromRef();
        PullRequestRef prrTo   = pr.getToRef();


        Repository repository = prrTo.getRepository();
        String url = "http://dockerlogin-eqx-01.force10networks.com:8080/mergeTrigger.php";
        String branch = prrTo.getDisplayId();
        String packageName = repository.getName();

        String keyPackageName  = repository.getProject().getKey();
        String fullPackageName = repository.getProject().getName();
        //String packageRevision = "4.0.0.84";
            if(!keyPackageName.toLowerCase().startsWith("ar")) {//this is not our repo, send it on its way
            log.error("this packageName.key doesn't start with ar");
            log.error(String.format("keyPackageName is equal to %s", keyPackageName.toLowerCase()));
            log.error(String.format("RepositoryHookResult is now accepted"));
            return ;//RepositoryHookResult.accepted();
        }

        long lpullRequestId = pr.getId();
        String pullRequestId = String.valueOf(lpullRequestId);
        String jsonContainer = String.format("description=%s", pr.getDescription());
        String mergeUser = "Administrator";
        //String newBranch = branch.replace("/", "!!!");
        //char ampresand = '&';

        String charset = "UTF-8";  // Or in Java 7 and later, use the constant: java.nio.charset.StandardCharsets.UTF_8.name()
        try {

            //URLConnection connection = new URL(url + "?" + query).openConnection();
            URLConnection connection = new URL(url).openConnection();
            HttpURLConnection http = (HttpURLConnection)connection;
            http.setRequestMethod("POST");
            http.setDoOutput(true);

            Map<String,String> arguments = new HashMap();
            arguments.put("my_branch", branch);
            arguments.put("packageName", packageName);
            arguments.put("packageRevision", "-1");
            arguments.put("jsonContainer", jsonContainer);
            arguments.put("mergeUser", mergeUser);
            arguments.put("pullRequestId", pullRequestId);

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
            StringBuilder returnResults = new StringBuilder();
            Boolean returnValue = false;
            while ((s = reader.readLine()) != null) {
                log.info(s);
                returnResults.append(s);
            }
            http.disconnect();
            if(returnResults.toString().contains("MergeCheckPassed")) {
                if (returnResults.toString().contains("succeeded")) {
                    log.error("returning success for merge, found MergeCheckPassed + succeeded");
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
        /*
        String summaryMsg = "com.dell.bitbucket.merge.check.summary:  " +
                "Only repository administrators may approve pull requests";
        //request.veto(summaryMsg, detailedMsg);
        */

        String summaryMsg  = "checking dell.engOps implementation";
        String detailedMsg = "com.dell.bitbucket.merge.check.detailed:  " +
                "The pull request must first pass the Continuum: Merge Trigger Build & the Merge Trigger Smoke Test prior to merging ";
        mr.veto (summaryMsg, detailedMsg);
        log.error(String.format("returning veto: %s:%s", summaryMsg, detailedMsg));
    }

}


/* PreRepositoryHook<RepositoryHookRequest>*/
/*
    @Nonnull
    @Override
    public RepositoryHookResult preUpdate(@Nonnull PreRepositoryHookContext context,
                                          @Nonnull RepositoryHookRequest request) {

        if(request.getTrigger() != PULL_REQUEST_MERGE)
            return RepositoryHookResult.accepted();
        else
            System.out.println("We caught a Pull Request Merge!!!!");
        // Find all refs that are about to be deleted
        //
        Set<String> deletedRefIds = request.getRefChanges().stream()
                .filter(refChange -> refChange.getType() == RefChangeType.DELETE)
                .map(refChange -> refChange.getRef().getId())
                .collect(Collectors.toSet());

        if (deletedRefIds.isEmpty()) {
            // nothing is going to be deleted, no problem
            return RepositoryHookResult.accepted();
        }

        // verify whether any of the refs are already in review
        PullRequestSearchRequest searchRequest = new PullRequestSearchRequest.Builder()
                .state(PullRequestState.OPEN)
                .fromRefIds(deletedRefIds)
                .fromRepositoryId(request.getRepository().getId())
                .build();

        Page<PullRequest> found = pullRequestService.search(searchRequest, PageUtils.newRequest(0, 1));
        if (found.getSize() > 0) {
            // found at least 1 open pull request from one of the refs that are about to be deleted
            PullRequest pullRequest = found.getValues().iterator().next();
            return RepositoryHookResult.rejected(
                    i18nService.getMessage("hook.guide.branchinreview.summary"),
                    i18nService.getMessage("hook.guide.branchinreview.details",
                            pullRequest.getFromRef().getDisplayId(), pullRequest.getId()));
        }
//
        return RepositoryHookResult.rejected("not gonna do it right now!!!");
    }
    */
/*
public class mergeTriggerCheck implements PreRepositoryHook<RepositoryHookRequest> //MergeRequestCheck,  RepositoryHookRequest,  RepositoryMergeRequestCheck, RepositorySettingsValidator//
{
    private static final Pattern NUMBER_PATTERN = Pattern.compile("[0-9]+");

    @Override
    public void preUpdate(@Nonnull PreRepositoryHookContext context,
                           @Nonnull RepositoryHookRequest hookRequest) {
        PullRequestMergeHookRequest prMergeHook = hookRequest.getTrigger();
    }
     @Override
    public void check(RepositoryMergeRequestCheckContext context)
    {
        int requiredReviewers = Integer.parseInt(context.getSettings().getString("reviewers"));
        int acceptedCount = 0;
        for (PullRequestParticipant reviewer : context.getMergeRequest().getPullRequest().getReviewers())
        {
            acceptedCount = acceptedCount + (reviewer.isApproved() ? 1 : 0);
        }
        if (acceptedCount < requiredReviewers)
        {
            context.getMergeRequest().veto("Not enough approved reviewers", acceptedCount + " reviewers have approved your pull request. You need " + requiredReviewers + " (total) before you may merge.");
        }
    }
    @Override
    public void check(@Nonnull MergeRequest request) {
        System.out.println(String.format("got here, request repository is %s", request.getPullRequest().getToRef().getRepository()));
    }

        @Override
    public void validate(Settings settings, SettingsValidationErrors errors, Repository repository)
    {
        String numReviewersString = settings.getString("reviewers", "0").trim();
        if (!NUMBER_PATTERN.matcher(numReviewersString).matches())
        {
            errors.addFieldError("reviewers", "Enter a number");
        }
        else if (Integer.parseInt(numReviewersString) <= 0)
        {
            errors.addFieldError("reviewers", "Number of reviewers must be greater than zero");
        }
    }

}
*/