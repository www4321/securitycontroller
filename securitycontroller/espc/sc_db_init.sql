use mysql;

INSERT INTO user(Host, User, Password) VALUES("%", "controller", password("sds"));
CREATE DATABASE IF NOT EXISTS securitycontroller;
GRANT ALL PRIVILEGES ON securitycontroller.* TO controller@'localhost' IDENTIFIED BY 'sds';
GRANT ALL PRIVILEGES ON securitycontroller.* TO controller@'%' IDENTIFIED BY 'sds';

use securitycontroller;

/*
Navicat MySQL Data Transfer

Source Server         : 10.65.100.136
Source Server Version : 50537
Source Host           : 10.65.100.136:3306
Source Database       : securitycontroller

Target Server Type    : MYSQL
Target Server Version : 50537
File Encoding         : 65001

Date: 2014-10-10 13:17:15
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for t_apps
-- ----------------------------
DROP TABLE IF EXISTS `t_apps`;
CREATE TABLE `t_apps` (
  `id` varchar(32) NOT NULL,
  `guid` varchar(32) NOT NULL,
  `name` varchar(32) NOT NULL,
  `alias` varchar(32) DEFAULT NULL,
  `hash` varchar(32) DEFAULT NULL,
  `version` varchar(16) DEFAULT NULL,
  `enable` tinyint(4) DEFAULT NULL,
  `protocol` varchar(10) DEFAULT NULL,
  `host` varchar(64) NOT NULL,
  `port` int(4) DEFAULT NULL,
  `root_url` varchar(32) DEFAULT NULL,
  `manage_url` varchar(128) DEFAULT NULL,
  `reg_time` int(4) DEFAULT NULL,
  `type` varchar(16) DEFAULT NULL,
  `category` varchar(16) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for t_devs
-- ----------------------------
DROP TABLE IF EXISTS `t_devs`;
CREATE TABLE `t_devs` (
  `id` varchar(32) NOT NULL,
  `vmid` varchar(32) DEFAULT NULL,
  `name` varchar(32) DEFAULT NULL,
  `alias` varchar(32) DEFAULT NULL,
  `hash` varchar(64) NOT NULL,
  `api_ver` varchar(16) DEFAULT NULL,
  `rule_ver` varchar(16) DEFAULT NULL,
  `ip` varchar(16) NOT NULL,
  `port` int(4) NOT NULL,
  `protocol` varchar(8) DEFAULT NULL,
  `root_url` varchar(64) DEFAULT NULL,
  `enable` tinyint(4) DEFAULT NULL,
  `reg_time` int(4) DEFAULT NULL,
  `manage_url` varchar(128) DEFAULT NULL,
  `license` varchar(16) DEFAULT NULL,
  `busy` tinyint(4) DEFAULT NULL,
  `type` varchar(16) DEFAULT NULL,
  `category` varchar(16) DEFAULT NULL,
  `mac_addrs` varchar(128) DEFAULT NULL,
  `service` varchar(64) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for t_realtime_basic
-- ----------------------------
DROP TABLE IF EXISTS `t_realtime_basic`;
CREATE TABLE `t_realtime_basic` (
  `obj_id` varchar(45) DEFAULT NULL,
  `type` varchar(10) DEFAULT NULL,
  `state` varchar(10) DEFAULT NULL,
  `update_time` int(11) DEFAULT NULL,
  `start_time` int(11) DEFAULT NULL,
  `cpu` int(11) DEFAULT NULL,
  `memory_used` int(11) DEFAULT NULL,
  `memory_total` int(11) DEFAULT NULL,
  `disk_used` int(11) DEFAULT NULL,
  `disk_total` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for t_secfunc_allocator_default
-- ----------------------------
DROP TABLE IF EXISTS `t_secfunc_allocator_default`;
CREATE TABLE `t_secfunc_allocator_default` (
  `device_id` varchar(32) NOT NULL,
  `device_type` varchar(32) NOT NULL,
  `protocol` varchar(8) NOT NULL,
  `ip` varchar(16) NOT NULL,
  `port` int(11) NOT NULL,
  `base_url` varchar(64) NOT NULL,
  `rest_version` varchar(32) NOT NULL,
  `reference` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for t_secfunc_allocator_webscan
-- ----------------------------
DROP TABLE IF EXISTS `t_secfunc_allocator_webscan`;
CREATE TABLE `t_secfunc_allocator_webscan` (
  `device_id` varchar(32) NOT NULL,
  `device_type` varchar(32) NOT NULL,
  `protocol` varchar(8) NOT NULL,
  `ip` varchar(16) NOT NULL,
  `port` int(11) NOT NULL,
  `base_url` varchar(64) NOT NULL,
  `rest_version` varchar(32) NOT NULL,
  `reference` varchar(11) NOT NULL,
  PRIMARY KEY (`device_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for t_secfunc_webprotect
-- ----------------------------
DROP TABLE IF EXISTS `t_secfunc_webprotect`;
CREATE TABLE `t_secfunc_webprotect` (
  `webprotect_key` varchar(128) NOT NULL,
  `device_id` varchar(32) NOT NULL,
  `app_id` varchar(16) NOT NULL,
  `tenant_id` varchar(16) NOT NULL,
  `website_domain` varchar(64) NOT NULL,
  `website_protocol` varchar(8) NOT NULL,
  `website_ip` varchar(16) NOT NULL,
  `website_port` int(11) NOT NULL,
  PRIMARY KEY (`webprotect_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for t_secfunc_webprotect_policy_template
-- ----------------------------
DROP TABLE IF EXISTS `t_secfunc_webprotect_policy_template`;
CREATE TABLE `t_secfunc_webprotect_policy_template` (
  `webprotect_key` varchar(128) NOT NULL,
  `policy_id` varchar(16) DEFAULT NULL,
  `template` varchar(1024) NOT NULL,
  `error_code` varchar(32) NOT NULL,
  `error_string` varchar(128) DEFAULT NULL,
  PRIMARY KEY (`webprotect_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for t_secfunc_webprotect_reverse_proxy
-- ----------------------------
DROP TABLE IF EXISTS `t_secfunc_webprotect_reverse_proxy`;
CREATE TABLE `t_secfunc_webprotect_reverse_proxy` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `webprotect_key` varchar(128) NOT NULL,
  `policy_id` varchar(16) DEFAULT NULL,
  `proxy_protocol` varchar(8) DEFAULT NULL,
  `proxy_ip` varchar(16) DEFAULT NULL,
  `proxy_port` int(11) DEFAULT NULL,
  `error_code` varchar(32) NOT NULL,
  `error_string` varchar(128) DEFAULT NULL,
  PRIMARY KEY (`id`,`webprotect_key`)
) ENGINE=InnoDB AUTO_INCREMENT=31 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for t_secfunc_webprotect_white_list
-- ----------------------------
DROP TABLE IF EXISTS `t_secfunc_webprotect_white_list`;
CREATE TABLE `t_secfunc_webprotect_white_list` (
  `white_list_key` varchar(134) NOT NULL,
  `webprotect_key` varchar(128) NOT NULL,
  `except_policy_id` varchar(16) NOT NULL,
  `site_id` varchar(16) NOT NULL,
  `src_ip` varchar(16) NOT NULL,
  `dst_port` varchar(8) NOT NULL,
  `domain` varchar(64) NOT NULL,
  `uri` varchar(4096) NOT NULL,
  `event_type` varchar(8) NOT NULL,
  `policy_id` varchar(16) NOT NULL,
  `rule_id` varchar(16) NOT NULL,
  PRIMARY KEY (`white_list_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for t_secfunc_webscan_task
-- ----------------------------
DROP TABLE IF EXISTS `t_secfunc_webscan_task`;
CREATE TABLE `t_secfunc_webscan_task` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `task_id` varchar(11) DEFAULT NULL,
  `app_id` varchar(16) NOT NULL,
  `tenant_id` varchar(16) NOT NULL,
  `device_id` varchar(32) NOT NULL,
  `protocol` varchar(8) NOT NULL,
  `host` varchar(64) NOT NULL,
  `port` int(11) NOT NULL,
  `scan_type` varchar(16) NOT NULL,
  `scan_type_parameter` varchar(256) DEFAULT NULL,
  `error_code` varchar(32) NOT NULL,
  `error_string` varchar(128) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for t_vms
-- ----------------------------
DROP TABLE IF EXISTS `t_vms`;
CREATE TABLE `t_vms` (
  `vmid` varchar(32) NOT NULL,
  `type` varchar(16) NOT NULL,
  `ip` varchar(16) NOT NULL,
  `status` varchar(16) DEFAULT NULL,
  `msg` varchar(64) DEFAULT NULL,
  `cfg_counter` int(4) DEFAULT '0',
  PRIMARY KEY (`vmid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

