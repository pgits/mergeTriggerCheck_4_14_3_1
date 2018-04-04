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
import java.util.StringJoiner;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.*;
//import com.atlassian.bitbucket.i18n.I18nService;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;


public class mergeTriggerCheck implements MergeRequestCheck {


    //private final I18nService i18nService;
    //private final PullRequestService pullRequestService;

    public mergeTriggerCheck(/*I18nService i18nService, PullRequestService pullRequestService*/) {
        //this.i18nService = i18nService;
        //this.pullRequestService = pullRequestService;
        System.out.println("initialized mergeTriggerCheck");
    }

    @Override
    public void check(@Nonnull MergeRequest request) {
        System.out.println(String.format("MergeRequest check called:  repo name = %s", request.getPullRequest().getToRef().getRepository().getId()));
        //TODO: implement me
        /*
        String summaryMsg = i18nService.getText("com.dell.bitbucket.merge.check.summary",
                "Only repository administrators may approve pull requests");
        String detailedMsg = i18nService.getText("com.dell.bitbucket.merge.check.detailed",
                "The user merging the pull request must be an administrator of the target repository");
        request.veto(summaryMsg, detailedMsg);
        */
        String summaryMsg = "com.dell.bitbucket.merge.check.summary:  " +
                "Only repository administrators may approve pull requests";
        String detailedMsg = "com.dell.bitbucket.merge.check.detailed:  " +
                "The user merging the pull request must be an administrator of the target repository";
        request.veto(summaryMsg, detailedMsg);
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