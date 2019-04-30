package org.sam.shen.scheduing.controller.portal;

import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.sam.shen.core.constants.Constant;
import org.sam.shen.core.model.Resp;
import org.sam.shen.scheduing.constants.SchedConstant;
import org.sam.shen.scheduing.entity.Agent;
import org.sam.shen.scheduing.entity.AgentGroup;
import org.sam.shen.scheduing.entity.RespPager;
import org.sam.shen.scheduing.entity.User;
import org.sam.shen.scheduing.service.AgentService;
import org.sam.shen.scheduing.service.RedisService;
import org.sam.shen.scheduing.vo.AgentEditVo;
import org.sam.shen.scheduing.vo.AgentGroupEditView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.github.pagehelper.Page;

import javax.servlet.http.HttpSession;

/**
 * @author suoyao
 * @date 2018年8月10日 上午11:22:38
  * 
 */
@Controller
@RequestMapping(value="portal")
public class AgentController {
	
	@Autowired
	private AgentService agentService;

    @Autowired
    private RedisService redisService;
	
	/**
	 * @author suoyao
	 * @date 上午11:22:35
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "agent", method = RequestMethod.GET)
	public ModelAndView toAgentPage(ModelAndView model) {
		model.setViewName("frame/agent/agent");
		return model;
	}
	
	/**
	 *  Agent 查询分页 JSON数据集合
	 * @author suoyao
	 * @date 上午11:22:12
	 * @param page
	 * @param limit
	 * @param agentName
	 * @return
	 */
	@RequestMapping(value = "agent/json-pager", method = RequestMethod.GET)
	@ResponseBody
	public RespPager<Page<Agent>> queryAgentForJsonPager(@RequestParam("page") Integer page,
                                                         @RequestParam("limit") Integer limit,
                                                         @RequestParam(value = "agentName", required = false, defaultValue = "") String agentName,
                                                         HttpSession session) {
		if(null == page) {
			page = 1;
		}
		if(null == limit) {
			limit = 10;
		}
        User user = (User) session.getAttribute("user");
		if (SchedConstant.ADMINISTRATOR.equals(user.getUname())) {
		    user.setId(null);
        }
		Page<Agent> pager = agentService.queryAgentForPager(page, limit, agentName, user.getId());
		return new RespPager<>(pager.getPageSize(), pager.getTotal(), pager);
	}
	
	/**
	 *  不分页的数据查询
	 * @author suoyao
	 * @date 下午3:45:35
	 * @param agentName
	 * @return
	 */
	@RequestMapping(value = "agent/json", method = RequestMethod.GET)
	@ResponseBody
	public Resp<List<Agent>> queryAgentForJson(
	        @RequestParam(value = "agentName", required = false, defaultValue = "") String agentName) {
		if (StringUtils.isEmpty(agentName)) {
			return new Resp<>(Collections.emptyList());
		}
		List<Agent> list = agentService.queryAgentForList(agentName, null);
		if(null == list) {
			list = Collections.emptyList();
		}
		return new Resp<>(list);
	} 
	
	/**
	 * Agent 编辑
	 * @author suoyao
	 * @date 上午11:21:39
	 * @param model
	 * @param id
	 * @return
	 */
	@RequestMapping(value = {"agent-edit/", "agent-edit/{id}"}, method = RequestMethod.GET)
	public ModelAndView agentEdit(ModelAndView model, @PathVariable(value = "id", required = false) Long id) {
		if(null != id) {
			model.addObject("view", agentService.agentEditView(id));
		} else {
			model.addObject("view", new AgentEditVo(new Agent(), Collections.emptyList()));
		}
		model.setViewName("frame/agent/agent_edit");
		return model;
	}
	
	/**
	 *  修改Agent客户端
	 * @author suoyao
	 * @date 下午4:32:22
	 * @param model
	 * @param agent
	 * @param handlers
	 * @return
	 */
	@RequestMapping(value = "agent-edit-save", method = RequestMethod.POST)
	public String agentEditSave(@ModelAttribute Agent agent,
	        @RequestParam(value = "handlers", required = false) List<String> handlers) {
		agentService.upgradeAgent(agent, handlers);
		return "redirect:/portal/agent-edit/" + agent.getId();
	}

    /**
     * 移除客户端信息
     * @author clock
     * @date 2019/4/30 上午11:09
     * @param agentId 客户端ID
     * @return 返回结果
     */
    @ResponseBody
	@RequestMapping(value = "agent/{agentId}", method = RequestMethod.DELETE)
	public Resp<String> deleteAgent(@PathVariable Long agentId) {
	    String redisKey = Constant.REDIS_AGENT_PREFIX.concat(Long.toString(agentId));
	    if (redisService.exists(redisKey)) {
	        return new Resp<>(Resp.FAIL.getCode(), "客户端正在使用中！");
        }
        agentService.removeAgent(agentId);
	    return Resp.SUCCESS;
    }

	@RequestMapping(value = "agent-group", method = RequestMethod.GET)
	public ModelAndView toAgentGroupPage(ModelAndView model, HttpSession session) {
	    User user = (User) session.getAttribute("user");
	    if (SchedConstant.ADMINISTRATOR.equals(user.getUname())) {
            model.setViewName("frame/agent/agent_group");
        } else {
	        model.setViewName("frame/agent/agent_group_select");
        }
		return model;
	}

	/**
	 *  查询AgentGroup Json数据集
	 * @author suoyao
	 * @date 下午4:58:24
	 * @return
	 */
	@RequestMapping(value = "agent-group/json", method = RequestMethod.GET)
	@ResponseBody
	public Resp<List<AgentGroup>> queryAgentGroupForJson(HttpSession session) {
	    User user = (User) session.getAttribute("user");
	    if (SchedConstant.ADMINISTRATOR.equals(user.getUname())) {
	        user.setId(null);
        }
		return new Resp<>(agentService.queryAgentGroup(user.getId()));
	}

    /**
     * 提供页面选择group使用
     * @author clock
     * @date 2019/2/26 下午3:36
     * @return 客户端组
     */
    @ResponseBody
    @RequestMapping(value = "agent-group-select", method = RequestMethod.GET)
    public List<Map<String, Object>> queryAgentGroup(@RequestParam(required = false) String groupName) {
	    List<AgentGroup> groups = agentService.queryAgentGroup(groupName);
	    return groups.stream().map(group -> {
	        Map<String, Object> map = new HashMap<>();
	        map.put("name", Long.toString(group.getId()).concat("-").concat(group.getGroupName()));
	        map.put("value", group.getId());
	        return map;
        }).collect(Collectors.toList());
    }
	
	/**
	 *  跳转到AgentGroup添加页面
	 * @author suoyao
	 * @date 下午4:33:16
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "agent-group-add", method = RequestMethod.GET)
	public ModelAndView agentGroupAdd(ModelAndView model) {
		model.setViewName("frame/agent/agent_group_add");
		return model;
	} 
	
	/**
	 *   编辑Agent Group
	 * @author suoyao
	 * @date 下午2:09:40
	 * @param model
	 * @param id
	 * @return
	 */
	@RequestMapping(value = { "agent-group-edit/", "agent-group-edit/{id}" }, method = RequestMethod.GET)
	public ModelAndView agentGroupEdit(ModelAndView model,
	        @PathVariable(value = "id", required = false) Long id) {
		if(null != id) {
			model.addObject("view", agentService.agentGroupEditView(id));
		} else {
			model.addObject("view", new AgentGroupEditView(new AgentGroup(), Collections.emptyList()));
		}
		model.setViewName("frame/agent/agent_group_edit");
		return model;
	}
	
	/**
	 *  保存Agent 机器组
	 * @author suoyao
	 * @date 上午10:05:35
	 * @param agentGroup
	 * @param agents
	 * @return
	 */
	@RequestMapping(value = "agent-group-save", method = RequestMethod.POST)
	public String agentGroupSave(@ModelAttribute AgentGroup agentGroup,
	        @RequestParam("agents") List<Long> agents) {
		agentGroup.setCreateTime(new Date());
		agentService.saveAgentGroup(agentGroup, agents);
		return "redirect:/portal/agent-group-edit/" + agentGroup.getId();
	}

    /**
     * 删除机器组
     * @author clock
     * @date 2019/3/12 下午1:33
     * @param id 机组ID
     * @return 删除结果
     */
	@RequestMapping(value = "agent-group-del", method = RequestMethod.POST)
	@ResponseBody
	public Resp<String> agentGroupDel(@RequestParam(value = "id", required = false) Long id) {
		if(null == id) {
			return Resp.FAIL;
		}
		agentService.deleteAgentGroup(id);
		return Resp.SUCCESS;
	}
	
}
