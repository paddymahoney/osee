/*
 * Created on Feb 25, 2009
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.framework.messaging;

/**
 * @author b1122182
 */
public interface IMessage {

   public void setHeader(IHeader header);

   public IHeader getHeader();

   public void setData(IData data);

   public IData getData();
}
