package com.example.newcode.controller;

import com.example.newcode.annotation.LoginRequired;
import com.example.newcode.entity.Event;
import com.example.newcode.event.EventProducer;
import com.example.newcode.service.LikeService;
import com.example.newcode.util.CommunityUtils;
import com.example.newcode.util.HostHolder;
import com.example.newcode.util.constant.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class LikeController implements CommunityConstant {

	@Autowired
	LikeService likeService;

	@Autowired
	HostHolder hostHolder;

	@Autowired
	private EventProducer eventProducer;

	/**
	 * 执行点赞或者取消点赞功能
	 *
	 * @param entityType
	 * @param entityId
	 * @param entityUserId
	 * @param postId
	 * @return
	 */
	@RequestMapping("/like")
	@ResponseBody
	@LoginRequired
	public String doLike(int entityType, int entityId, int entityUserId, int postId) {
		int id = hostHolder.getUser().getId();
		// 执行点赞或取消，之后还需要把更新的信息通过异步再传递会前端
		likeService.like(id, entityType, entityId, entityUserId);

		// 获取点赞数量用于异步返回
		long entityLikeCount = likeService.findEntityLikeCount(entityType, entityId);

		// 当前用户点赞状态用于异步返回
		int likeStatus = likeService.findEntityLikeStatus(id, entityType, entityId);

		Map<String, Object> map = new HashMap<>();
		map.put("likeCount", entityLikeCount);
		map.put("likeStatus", likeStatus);

		// 只有点赞才会触发时间
		// 触发点赞事件
		if (likeStatus == 1) {
			Event event = new Event().setTopic(TOPIC_LIKE). // 点赞事件
					setUserId(hostHolder.getUser().getId()). // 点赞的人的id
					setEntityType(entityType). // 点赞的目标类别 1是帖子 2是comment
					setEntityId(entityId). // 帖子id或者commetn的Id
					setEntityUserId(entityUserId). // 被点赞的用户id
					setData("postId", postId); // 点赞只能对帖子或者comment点赞，这都是属于一个帖子下的，所以为了跳转带上postId
			eventProducer.fireEvent(event);
		}
		return CommunityUtils.getJSONString(0, null, map);
	}

}
