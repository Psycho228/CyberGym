-- Add primary_goals column to profiles table
ALTER TABLE profiles ADD COLUMN IF NOT EXISTS primary_goals TEXT[] DEFAULT '{}';
