#include "stdafx.h"
#include "../inc/Reserved.h"

/*******************************************************************************
* Function Name  : toPrintConf
* Description    : Make the print configuration since using reserved[0] and 
				   reserved[1] to represent it.
* Input          : reserved[] - the 4-byte char array for the reserved
* Output         : None
* Return         : The print conf value combined with reserved[0] and reserved[1]
*******************************************************************************/
int Reserved::toPrintConf(char reserved[]){
	return (reserved[0] & 0x000000FF) | ((reserved[1] & 0x000000FF) << 8);
}