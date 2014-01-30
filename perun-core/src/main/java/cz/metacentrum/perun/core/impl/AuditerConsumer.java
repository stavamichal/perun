package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.api.AuditMessage;
import cz.metacentrum.perun.core.api.Pair;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;

/**
* 
*
* @author Slavek Licehammer
*/
public class AuditerConsumer {

  private final static Logger log = LoggerFactory.getLogger(AuditerConsumer.class);

  private SimpleJdbcTemplate jdbc;
  private int lastProcessedId = 0; //id of last processed message
  private String consumerName;

  /**
   * Auditer log mapper
   */
  private static final RowMapper<String> AUDITER_LOG_MAPPER = new RowMapper<String>() {
    public String mapRow(ResultSet rs, int i) throws SQLException {
      AuditMessage auditMessage = Auditer.AUDITMESSAGE_MAPPER.mapRow(rs, i);
      return auditMessage.getMsg();
    }
  };
  
  private static final RowMapper<String> AUDITER_LOG_MAPPER_FOR_PARSER = new RowMapper<String>() {
    public String mapRow(ResultSet rs, int i) throws SQLException {
      AuditMessage auditMessage = Auditer.AUDITMESSAGE_MAPPER_FOR_PARSER.mapRow(rs, i);
      return auditMessage.getMsg();
    }
  };
  
  private static final RowMapper<Pair<String,Integer>> AUDITER_LOG_MAPPER_FOR_PARSER_WITH_ID = new RowMapper<Pair<String, Integer>>() {
    public Pair<String, Integer> mapRow(ResultSet rs, int i) throws SQLException {
      AuditMessage auditMessage = Auditer.AUDITMESSAGE_MAPPER_FOR_PARSER.mapRow(rs, i);
      return new Pair(auditMessage.getMsg(), auditMessage.getId());
    }
  };

  public AuditerConsumer(String consumerName, DataSource perunPool) throws InternalErrorException {
    this.jdbc = new SimpleJdbcTemplate(perunPool);
    this.consumerName = consumerName;
    try {
      this.lastProcessedId = jdbc.queryForInt("select last_processed_id from auditer_consumers where name=?", consumerName);
    } catch(EmptyResultDataAccessException ex) {
      
      try {
        String dbType = Utils.getPropertyFromConfiguration("perun.perun.db.type");
        if(!dbType.equals("master")) {
          log.debug("DB-Slave: This machine is probably slave so can't set lastProcessedId if is not in DB.");
          return;
        }
      } catch (Exception e) {
        //If exists some problem with property file, do like this is master, only log it
        log.error("Property file reading perun.perun.db.type error.", e);
      }
      
      //listenerName doesn't have record in auditer_consumers 
      try {
        // New listener, set the lastProcessedId to the latest one
        lastProcessedId = jdbc.queryForInt("select max(id) from auditer_log");

        int consumerId = Utils.getNewId(jdbc, "auditer_consumers_id_seq");
        jdbc.update("insert into auditer_consumers (id, name, last_processed_id) values (?,?,?)", consumerId, consumerName, lastProcessedId);
        log.debug("New consumer [name: '{}', lastProcessedId: '{}'] created.", consumerName, lastProcessedId);
      } catch(Exception e) {
        throw new InternalErrorException(e);
      }
      
    } catch(Exception ex) {
      throw new InternalErrorException(ex);
    }
  }

  public List<String> getMessages() throws InternalErrorException {
    try {
      int maxId = jdbc.queryForInt("select max(id) from auditer_log");
      if(maxId > lastProcessedId) {
        List<String> messages = jdbc.query("select " + Auditer.auditMessageMappingSelectQuery + " from auditer_log where id > ? and id <= ? order by id", AUDITER_LOG_MAPPER, this.lastProcessedId, maxId);
        this.lastProcessedId = maxId;
        jdbc.update("update auditer_consumers set last_processed_id=? where name=?", this.lastProcessedId,this.consumerName);
        return messages;
      }
      return new ArrayList<String>();
    } catch(Exception ex) {
      throw new InternalErrorException(ex);
    }
  }
  
  public List<String> getMessagesForParser() throws InternalErrorException {
    try {
      int maxId = jdbc.queryForInt("select max(id) from auditer_log");
      if(maxId > lastProcessedId) {
        List<String> messages = jdbc.query("select " + Auditer.auditMessageMappingSelectQuery + " from auditer_log where id > ? and id <= ? order by id", AUDITER_LOG_MAPPER_FOR_PARSER, this.lastProcessedId, maxId);
        this.lastProcessedId = maxId;
        jdbc.update("update auditer_consumers set last_processed_id=? where name=?", this.lastProcessedId,this.consumerName);
        return messages;
      }
      return new ArrayList<String>();
    } catch(Exception ex) {
      throw new InternalErrorException(ex);
    }    
  }
  
  public List<Pair<String, Integer>> getMessagesForParserLikePairWithId() throws InternalErrorException {
    try {
      int maxId = jdbc.queryForInt("select max(id) from auditer_log");
      if(maxId > lastProcessedId) {
        List<Pair<String, Integer>> messages = jdbc.query("select " + Auditer.auditMessageMappingSelectQuery + " from auditer_log where id > ? and id <= ? order by id", AUDITER_LOG_MAPPER_FOR_PARSER_WITH_ID, this.lastProcessedId, maxId);
        this.lastProcessedId = maxId;
        jdbc.update("update auditer_consumers set last_processed_id=? where name=?", this.lastProcessedId,this.consumerName);
        return messages;
      }
      return new ArrayList<Pair<String, Integer>>();
    } catch(Exception ex) {
      throw new InternalErrorException(ex);
    }    
  }
}