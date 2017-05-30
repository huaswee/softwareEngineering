-- phpMyAdmin SQL Dump
-- version 4.1.14
-- http://www.phpmyadmin.net
--
-- Host: 127.0.0.1
-- Generation Time: Nov 15, 2015 at 09:21 AM
-- Server version: 5.6.17
-- PHP Version: 5.5.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Database: `se`
--

-- --------------------------------------------------------

--
-- Table structure for table `admin`
--

DROP TABLE IF EXISTS `admin`;
CREATE TABLE IF NOT EXISTS `admin` (
  `username` varchar(40) CHARACTER SET latin1 COLLATE latin1_general_cs NOT NULL,
  `password` varchar(40) CHARACTER SET latin1 COLLATE latin1_general_cs NOT NULL,
  PRIMARY KEY (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `app`
--

DROP TABLE IF EXISTS `app`;
CREATE TABLE IF NOT EXISTS `app` (
  `timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `mac_address` char(40) CHARACTER SET latin1 COLLATE latin1_general_ci NOT NULL,
  `app_id` int(11) NOT NULL,
  PRIMARY KEY (`timestamp`,`mac_address`),
  UNIQUE KEY `app_mac_timestamp` (`mac_address`,`timestamp`),
  UNIQUE KEY `app_mac_timestamp_appid` (`mac_address`,`timestamp`,`app_id`),
  KEY `applookup_app_fk` (`app_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `applookup`
--

DROP TABLE IF EXISTS `applookup`;
CREATE TABLE IF NOT EXISTS `applookup` (
  `app_id` int(11) NOT NULL,
  `app_name` varchar(60) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
  `app_category` varchar(20) CHARACTER SET latin1 COLLATE latin1_general_ci NOT NULL,
  PRIMARY KEY (`app_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `demographics`
--

DROP TABLE IF EXISTS `demographics`;
CREATE TABLE IF NOT EXISTS `demographics` (
  `mac_address` char(40) CHARACTER SET latin1 COLLATE latin1_general_ci NOT NULL,
  `name` varchar(60) CHARACTER SET latin1 COLLATE latin1_general_cs NOT NULL,
  `password` varchar(40) CHARACTER SET latin1 COLLATE latin1_general_cs NOT NULL,
  `email` varchar(50) CHARACTER SET latin1 COLLATE latin1_general_ci NOT NULL,
  `gender` char(1) CHARACTER SET latin1 COLLATE latin1_general_ci NOT NULL,
  `cca` varchar(63) CHARACTER SET latin1 COLLATE latin1_general_ci NOT NULL,
  PRIMARY KEY (`mac_address`),
  UNIQUE KEY `demo_mac_email_gender_cca` (`mac_address`,`email`,`gender`,`cca`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `app`
--
ALTER TABLE `app`
  ADD CONSTRAINT `app_applookup_FK1` FOREIGN KEY (`app_id`) REFERENCES `applookup` (`app_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `app_demo_FK1` FOREIGN KEY (`mac_address`) REFERENCES `demographics` (`mac_address`) ON DELETE CASCADE ON UPDATE CASCADE;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
