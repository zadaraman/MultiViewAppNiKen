package com.nexstreaming.nexplayerengine;

import java.util.ArrayList;

/**
 * \brief This class allows an application to retrieve host information by using the hostname and its corresponding custom IP address registered by the developer.
 * 
 * \since version 6.38
 */
public class NexNetAddrTable {
	

	/**
	 * This is a possible table type for the method \c setNetAddrTable.
	 *
	 * When the parameter \c nNetAddrTableType in \c NexPlayer.setNetAddrTable is set to this table type, the method will try to get the host information 
	 * using the hostname and its corresponding custom IP address, set by the developer, from the table registered as the parameter \c table.  If this fails,
	 * the player will try to get the host information from the existing host database. 
	 * 
	 * In other words, this means that the information in the table registered will be used with priority, overriding the information from the existing host database.
	 *
	 * \since version 6.38
	 *
	 */
	public static final int NETADDR_TABLE_OVERRIDE = 0;

	/**
	 * This is a possible table type for the method \c setNetAddrTable.
	 * 
	 * When the parameter \c nNetAddrTableType in \c NexPlayer.setNetAddrTable is set to this table type, the method will try 
	 * to retrieve the host information from the existing host database using the given hostname.  If this fails, the method will 
	 * check the table registered as the parameter \c table to retrieve the custom IP address corresponding to the given hostname.
	 * 
	 * In other words, this means that the table of net addresses can be used as a fallback option when the needed host information cannot be retrieved.
	 *
	 * \since version 6.38
	 */
	public static final int NETADDR_TABLE_FALLBACK = 1;


	/**
	 * This indicates the maximum number of entries that can be set in the table of customized IP addresses.  
	 *
	 * \since version 6.38 
	 */
	private static final int NETADDR_TABLE_MAX_SIZE = 5;

	private static final String LOG_TAG = "NEX_NETADDRTABLE_JAVA";

	private ArrayList<NetAddrTableInfo> mArrNetAddrInfo = new ArrayList<NetAddrTableInfo>();
 
/**
* \brief  This class defines the information possible for an entry made with the method \c addEntry, including the hostname and its corresponding IP address when
* a customized IP address is desired.   
*
* \since version 6.38
*/
	public class NetAddrTableInfo
	{
		private String mHostName;
		private String mAddress;

		NetAddrTableInfo(String hostname, String address)
		{
			mHostName = hostname;
			mAddress = address;
		}
	}

	/**
	* \brief  This method adds an entry to the table of customized IP addresses.
	* 
	* An entry includes a hostname and its corresponding custom IP address.  Depending on the table type set with the parameter \c nNetAddrTableType 
	* when calling \c NexPlayer.setNetAddrTable, the IP address corresponding to a particular hostname will be used to retrieve host information.  
	* 
	* This table can be used as a fallback option (\c NETADDR_TABLE_FALLBACK type) to retrieve host information, or it can be 
	* used to override (\c NETADDR_TABLE_OVERRIDE type) the information in the existing host database.
	* 
	* A maximum of 5 entries can be assigned to a table. 
	* 
	* \param hostname    The hostname as a \c String.
	* \param address     The custom IP address corresponding to the hostname set by the developer as a \c String. 
	*
	* \return 1 if successful, otherwise 0. 
	* 
	* \since version 6.38
	*/

	public boolean addEntry(String hostname, String address)
	{
		boolean bAdd = false;

		if (NETADDR_TABLE_MAX_SIZE >= mArrNetAddrInfo.size())
		{
			bAdd = mArrNetAddrInfo.add(new NetAddrTableInfo(hostname, address));
		}
		else
		{
			NexLog.d(LOG_TAG, "NexNetAddrTable is fulled");
		}

		return bAdd;
	}
}