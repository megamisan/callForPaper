/*
 * Copyright (c) 2016 BreizhCamp
 * [http://breizhcamp.org]
 *
 * This file is part of CFP.io.
 *
 * CFP.io is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package io.cfp.service;

import io.cfp.dto.CommentUser;
import io.cfp.entity.Comment;
import io.cfp.entity.Event;
import io.cfp.entity.Talk;
import io.cfp.repository.CommentRepo;
import io.cfp.repository.TalkRepo;
import ma.glasnost.orika.MapperFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * Service for managing comment by the user
 */
@Service
@Transactional
public class CommentUserService {

    @Autowired
    private TalkRepo talkRepo;

    @Autowired
    private CommentRepo commentRepo;

    @Autowired
    private MapperFacade mapper;

    /**
     * Retrieve all comments for the talk which belong to the user
     * @param userId User of the talk
     * @param talkId Talk to retrieve comments from
     * @return List of comments
     */
    public List<CommentUser> findAll(int userId, int talkId) {
        List<Comment> comments = commentRepo.findByTalkForUser(talkId, userId, Event.current());
        return mapper.mapAsList(comments, CommentUser.class);
    }

    /**
     * Add a comment to a talk
     * @param userId User of the talk
     * @param talkId Talk to associated with the comment
     * @param commentUser Comment to add
     * @return Added comment or null if talk doesn't exists for this user
     */
    public CommentUser addComment(int userId, int talkId, CommentUser commentUser) {
        Talk talk = talkRepo.findByIdAndEventIdAndUserId(talkId, Event.current(), userId);
        if (talk == null) return null;

        Comment comment = mapper.map(commentUser, Comment.class);
        comment.setAdded(new Date());
        comment.setTalk(talk);
        comment.setUser(talk.getUser());
        comment.setInternal(false);

        Comment saved = commentRepo.save(comment);
        commentRepo.flush();

        return mapper.map(saved, CommentUser.class);
    }

    /**
     * Edit a comment associated to a talk
     * @param userId User of the talk
     * @param talkId Talk to associated with the comment
     * @param commentUser Comment to edit
     * @return Edited comment or null if talk doesn't exists for this user
     */
    public CommentUser editComment(int userId, int talkId, CommentUser commentUser) {
        Comment comment = commentRepo.findByIdForTalkAndUser(commentUser.getId(), talkId, userId, Event.current());
        if (comment == null) return null;
        if (comment.getUser() != null && comment.getUser().getId() !=  userId) return null; //cannot edit a comment posted by an other user

        mapper.map(commentUser, comment);
        commentRepo.flush();

        return mapper.map(comment, CommentUser.class);
    }
}
